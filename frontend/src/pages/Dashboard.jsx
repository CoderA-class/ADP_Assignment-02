import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../components/Icons';
import { BarChart, Donut } from '../components/Charts';
import { Avatar, Badge, EmptyState, ErrorState, Loading, StatCard } from '../components/ui';
import NoticeBoard from '../components/NoticeBoard';
import { useAuth } from '../context/AuthContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { categoryColor, categoryIcon, colorFor, daysSince, formatDate, fullName, money, number, stockStatus, toDate } from '../utils/format';

export default function Dashboard() {
  const { user, isAdmin } = useAuth();
    const { data, loading, error, reload } = useApi(async () => {
    const [products, customers, orders, leave, employees] = await Promise.all([
      api.products.all(), api.customers.all(), api.orders.all(),
      isAdmin ? api.leave.all() : api.leave.forEmployee(user.empID),
      isAdmin ? api.employees.all() : Promise.resolve([])
    ]);
    return { products, customers, orders, leave, employees };
  }, [isAdmin, user.empID]);

  const stats = useMemo(() => {
    if (!data) return null;
    const { products, customers, orders } = data;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
    const sameDay = (a, b) => a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();

    const todaysOrders = orders.filter((o) => toDate(o.orderDate) && sameDay(toDate(o.orderDate), today));
    const monthOrders = orders.filter((o) => toDate(o.orderDate) >= monthStart);
    const days = [...Array(14)].map((_, i) => {
      const d = new Date(today); d.setDate(d.getDate() - (13 - i));
      const value = orders.filter((o) => toDate(o.orderDate) && sameDay(toDate(o.orderDate), d)).reduce((s, o) => s + o.totalAmount, 0);
      return { label: d.toLocaleDateString('en-ZA', { day: 'numeric', month: 'short' }), full: d.toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long' }), value, highlight: i === 13 };
    });

    const low = products.filter((p) => stockStatus(p).key !== 'ok').sort((a, b) => a.quantity - b.quantity);
    const stockValue = products.reduce((s, p) => s + p.price * p.quantity, 0);

    const productSales = {};
    orders.forEach((o) => (o.orderItems || []).forEach((it) => {
      const key = it.productId;
      productSales[key] = productSales[key] || { id: key, name: it.productName || products.find((p) => p.id === key)?.name || key, qty: 0, revenue: 0 };
      productSales[key].qty += it.quantity;
      productSales[key].revenue += it.lineTotal;
    }));
    const top = Object.values(productSales).sort((a, b) => b.revenue - a.revenue).slice(0, 5);

    const categories = {};
    products.forEach((p) => { categories[p.category || 'General'] = (categories[p.category || 'General'] || 0) + 1; });
    const catData = Object.entries(categories).sort((a, b) => b[1] - a[1]).slice(0, 6).map(([label, value]) => ({ label, value, color: categoryColor(label) }));

    const inactive = customers.filter((c) => {
      const d = daysSince(c.lastPurchaseDate || c.createdDate);
      return d !== null && d > 30;
    });

    const yesterday = new Date(today); yesterday.setDate(today.getDate() - 1);
    const yRevenue = orders.filter((o) => toDate(o.orderDate) && sameDay(toDate(o.orderDate), yesterday)).reduce((s, o) => s + o.totalAmount, 0);
    const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const lastMonthSameDay = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate() + 1);
    const lmRevenue = orders.filter((o) => { const d = toDate(o.orderDate); return d >= lastMonthStart && d < lastMonthSameDay; }).reduce((s, o) => s + o.totalAmount, 0);
    const pct = (now, before) => (before > 0 ? ((now - before) / before) * 100 : now > 0 ? Infinity : 0);
    const todayRevenue = todaysOrders.reduce((s, o) => s + o.totalAmount, 0);
    const monthRevenue = monthOrders.reduce((s, o) => s + o.totalAmount, 0);
    const ordersPerDay = [...Array(14)].map((_, i) => {
      const d = new Date(today); d.setDate(d.getDate() - (13 - i));
      return orders.filter((o) => toDate(o.orderDate) && sameDay(toDate(o.orderDate), d)).length;
    });

    return {
      todayTrend: pct(todayRevenue, yRevenue), monthTrend: pct(monthRevenue, lmRevenue), ordersPerDay,
      avgOrder: monthOrders.length ? monthRevenue / monthOrders.length : 0,
      todayRevenue, todayCount: todaysOrders.length,
      monthRevenue, monthCount: monthOrders.length,
      days, low, stockValue, top, catData, inactive,
      recent: [...orders].sort((a, b) => String(b.orderTime || b.orderDate).localeCompare(String(a.orderTime || a.orderDate))).slice(0, 6)
    };
  }, [data]);

  if (loading) return <Loading label="Loading your dashboard…" />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const pendingLeave = data.leave.filter((l) => l.status === 'PENDING');
  const autoNotes = [];
  if (stats.low.length) autoNotes.push({ key: 'low', color: 'peach', icon: 'alert', title: 'Restock soon', text: `${stats.low.slice(0, 3).map((p) => p.name).join(', ')}${stats.low.length > 3 ? ` and ${stats.low.length - 3} more` : ''} ${stats.low.length === 1 ? 'is' : 'are'} at or below the re-order level.`, link: '/products', state: { filter: 'low' }, linkLabel: 'Restock list' });
  if (isAdmin && pendingLeave.length) autoNotes.push({ key: 'leave', color: 'mint', icon: 'calendar', title: 'Leave to approve', text: `${pendingLeave.length} request${pendingLeave.length === 1 ? '' : 's'} from ${[...new Set(pendingLeave.map((l) => l.employeeName))].slice(0, 2).join(' & ')}.`, link: '/leave', linkLabel: 'Review' });
  if (!isAdmin && pendingLeave.length) autoNotes.push({ key: 'myleave', color: 'mint', icon: 'calendar', title: 'Leave pending', text: `You have ${pendingLeave.length} leave request${pendingLeave.length === 1 ? '' : 's'} waiting for approval.`, link: '/leave', linkLabel: 'View' });
  if (stats.inactive.length) autoNotes.push({ key: 'inactive', color: 'lilac', icon: 'phone', title: 'Give them a call', text: `${stats.inactive.length} customer${stats.inactive.length === 1 ? " hasn't" : "s haven't"} bought anything in over 30 days.`, link: '/customers', linkLabel: 'Customers' });
  if (!autoNotes.length) autoNotes.push({ key: 'ok', color: 'sky', icon: 'smile', title: 'All clear', text: 'No stock, leave or customer alerts right now. Nice work!' });
  const greeting = new Date().getHours() < 12 ? 'Good morning' : new Date().getHours() < 17 ? 'Good afternoon' : 'Good evening';

  return (
    <div className="stack" style={{ gap: 20 }}>
      <section className="welcome">
        <div>
          <div className="date">{new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</div>
          <h2>{greeting}, <span className="greet-name">{user.firstName === 'System' ? 'Admin' : user.firstName}</span></h2>
          <p>
            {stats.todayCount ? `${stats.todayCount} sale${stats.todayCount === 1 ? '' : 's'} rung up so far today.` : 'No sales yet today.'}{' '}
            {stats.low.length ? `${stats.low.length} product${stats.low.length === 1 ? ' needs' : 's need'} restocking` : 'Stock levels look healthy'}
            {isAdmin && pendingLeave.length ? ` and ${pendingLeave.length} leave request${pendingLeave.length === 1 ? ' is' : 's are'} waiting for you.` : '.'}
          </p>
        </div>
      </section>

      <div className="quick-actions">
        <Link to="/pos" className="quick-action qa-coral"><div className="qa-ico c-coral"><Icon name="pos" size={20} /></div><div><div className="qa-t">New sale</div><div className="qa-s">Ring up an order</div></div></Link>
        <Link to="/products" state={{ filter: 'low' }} className="quick-action qa-amber"><div className="qa-ico c-amber"><Icon name="dolly" size={20} /></div><div><div className="qa-t">Restock list</div><div className="qa-s">{stats.low.length ? `${stats.low.length} items running low` : 'All stocked up'}</div></div></Link>
        {isAdmin
          ? <Link to="/customers" className="quick-action qa-lilac"><div className="qa-ico c-lilac"><Icon name="userPlus" size={20} /></div><div><div className="qa-t">Customers</div><div className="qa-s">{stats.inactive.length} to follow up</div></div></Link>
          : <Link to="/leave" className="quick-action qa-mint"><div className="qa-ico c-mint"><Icon name="calendar" size={20} /></div><div><div className="qa-t">Request leave</div><div className="qa-s">{pendingLeave.length ? `${pendingLeave.length} pending` : 'Plan time off'}</div></div></Link>}
        {isAdmin
          ? <Link to="/leave" className="quick-action qa-mint"><div className="qa-ico c-mint"><Icon name="calendarCheck" size={20} /></div><div><div className="qa-t">Leave requests</div><div className="qa-s">{pendingLeave.length ? `${pendingLeave.length} waiting` : 'Nothing waiting'}</div></div></Link>
          : <Link to="/attendance" className="quick-action qa-yellow"><div className="qa-ico c-yellow"><Icon name="userClock" size={20} /></div><div><div className="qa-t">My shifts</div><div className="qa-s">Clock-in history</div></div></Link>}
      </div>

      <div className="grid grid-4">
        <StatCard icon="money" tone="cyan" label="Sales today" value={money(stats.todayRevenue)} trend={stats.todayTrend} trendLabel="vs yesterday" />
        <StatCard icon="chartLine" tone="blue" label="This month" value={money(stats.monthRevenue)} trend={stats.monthTrend} trendLabel="vs last month" />
        <StatCard icon="receipt" tone="violet" label="Orders today" value={stats.todayCount} spark={stats.ordersPerDay} sparkColor="var(--violet)" foot={`Avg. order ${money(stats.avgOrder)}`} />
        <StatCard icon="alert" tone={stats.low.length ? 'amber' : 'green'} label="Low / out of stock" value={stats.low.length} foot={stats.low.length ? 'Needs attention' : 'All good'} />
      </div>

      <div className="card">
        <div className="card-header">
          <div><h3>Notice board</h3><div className="sub">Alerts update automatically. Your own notes are saved on this computer.</div></div>
        </div>
        <div className="board">
          <NoticeBoard userId={user.empID} autoNotes={autoNotes} />
        </div>
      </div>

      <div className="grid grid-dash">
        <div className="card">
          <div className="card-header">
            <div><h3>Sales – last 14 days</h3><div className="sub">Total value of orders per day</div></div>
            <Badge tone="blue" plain>{money(stats.days.reduce((s, d) => s + d.value, 0))}</Badge>
          </div>
          <div className="card-body">
            <BarChart data={stats.days} />
          </div>
        </div>
        <div className="card">
          <div className="card-header"><h3>Low stock</h3><Link to="/products" className="small">View all</Link></div>
          <div className="card-body flush">
            {stats.low.length === 0 ? <EmptyState icon="checkCircle" title="Stock levels look good" message="Nothing is below its re-order level." />
              : stats.low.slice(0, 6).map((p) => {
                const s = stockStatus(p);
                return (
                  <div className="list-row" key={p.id}>
                    <Avatar src={p.imageUrl} name={p.name} tint={categoryColor(p.category)} icon={categoryIcon(p.category)} size={36} square />
                    <div className="grow">
                      <div className="t">{p.name}</div>
                      <div className="s">{p.quantity} left · re-order at {p.reorderLevel}</div>
                    </div>
                    <Badge tone={s.tone}>{s.label}</Badge>
                  </div>
                );
              })}
          </div>
        </div>
      </div>

      <div className="grid grid-3">
        <div className="card">
          <div className="card-header"><h3>Recent orders</h3><Link to="/orders" className="small">View all</Link></div>
          <div className="card-body flush">
            {stats.recent.length === 0 ? <EmptyState icon="receipt" title="No sales yet" message="Completed sales will show up here." />
              : stats.recent.map((o) => (
                <div className="list-row" key={o.orderId}>
                  <Avatar name={o.customerName || o.customerId} size={34} />
                  <div className="grow">
                    <div className="t">{o.customerName || o.customerId}</div>
                    <div className="s"><span className="mono">{o.orderId}</span> · {formatDate(o.orderDate)}</div>
                  </div>
                  <div className="bold tabular">{money(o.totalAmount)}</div>
                </div>
              ))}
          </div>
        </div>
        <div className="card">
          <div className="card-header"><h3>Top products</h3><span className="sub">by sales value</span></div>
          <div className="card-body">
            {stats.top.length === 0 ? <p className="muted">No sales yet.</p> : (
              <div className="stack" style={{ gap: 14 }}>
                {stats.top.map((t) => (
                  <div key={t.id}>
                    <div className="row-flex between small"><span className="bold" style={{ fontSize: 13 }}>{t.name}</span><span className="tabular">{money(t.revenue)}</span></div>
                    <div className="progress mt-1"><span style={{ width: `${(t.revenue / stats.top[0].revenue) * 100}%`, background: colorFor(t.name) }} /></div>
                    <div className="small muted mt-1">{t.qty} units sold</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        <div className="card">
          <div className="card-header"><h3>Products by category</h3></div>
          <div className="card-body">
            {stats.catData.length === 0 ? <p className="muted">No products yet.</p>
              : <Donut data={stats.catData} centerValue={data.products.length} centerLabel="products" />}
          </div>
        </div>
      </div>

      {isAdmin ? (
        <div className="grid grid-2">
          <div className="card">
            <div className="card-header"><h3>Pending leave requests</h3><Link to="/leave" className="small">Review</Link></div>
            <div className="card-body flush">
              {pendingLeave.length === 0 ? <EmptyState icon="calendar" title="No pending requests" /> : pendingLeave.slice(0, 5).map((l) => (
                <div className="list-row" key={l.leaveId}>
                  <Avatar src={data.employees.find((e) => e.empID === l.employeeId)?.imageUrl} name={l.employeeName} size={34} />
                  <div className="grow">
                    <div className="t">{l.employeeName}</div>
                    <div className="s">{formatDate(l.startDate)} – {formatDate(l.endDate)} · {l.days} day{l.days === 1 ? '' : 's'}</div>
                  </div>
                  <Badge tone="amber">Pending</Badge>
                </div>
              ))}
            </div>
          </div>
          <div className="card">
            <div className="card-header"><h3>Customers to follow up</h3><span className="sub">No purchase in 30+ days</span></div>
            <div className="card-body flush">
              {stats.inactive.length === 0 ? <EmptyState icon="users" title="Everyone's active" /> : stats.inactive.slice(0, 5).map((c) => (
                <div className="list-row" key={c.custID}>
                  <Avatar src={c.imageUrl} name={fullName(c)} size={34} />
                  <div className="grow">
                    <div className="t">{fullName(c)}</div>
                    <div className="s">{c.company || c.phoneNumber || c.email || c.custID}</div>
                  </div>
                  <Badge tone="violet">{c.lastPurchaseDate ? `${daysSince(c.lastPurchaseDate)} days` : 'Never bought'}</Badge>
                </div>
              ))}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
