import { useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import { BarChart, Donut, Sparkline } from '../components/Charts';
import { Avatar, Badge, EmptyState, ErrorState, Loading, StatCard, TrendChip } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { categoryColor, PALETTE, colorFor, daysSince, formatDate, fullName, money, number, stockStatus, toDate } from '../utils/format';

export default function Dashboard() {
  const { user, isAdmin } = useAuth();
  const navigate = useNavigate();
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
  const greeting = new Date().getHours() < 12 ? 'Good morning' : new Date().getHours() < 17 ? 'Good afternoon' : 'Good evening';

  return (
    <div className="stack" style={{ gap: 20 }}>
      <section className="hero">
        <div style={{ position: 'relative', zIndex: 1 }}>
          <div className="hero-date">{new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</div>
          <h2>{greeting}, {user.firstName}.</h2>
          <p>
            {stats.todayCount ? `${stats.todayCount} sale${stats.todayCount === 1 ? '' : 's'} rung up so far today.` : 'No sales yet today.'}{' '}
            {stats.low.length ? `${stats.low.length} product${stats.low.length === 1 ? ' needs' : 's need'} restocking` : 'Stock levels look healthy'}
            {isAdmin && pendingLeave.length ? ` and ${pendingLeave.length} leave request${pendingLeave.length === 1 ? ' is' : 's are'} waiting for you.` : '.'}
          </p>
          <div className="hero-actions">
            <button className="btn btn-accent" onClick={() => navigate('/pos')}><Icon name="cart" size={16} /> New sale</button>
            <button className="btn btn-ghost-light" onClick={() => navigate('/products', { state: { filter: 'low' } })}><Icon name="box" size={16} /> Restock list</button>
            {isAdmin && pendingLeave.length > 0 && <button className="btn btn-ghost-light" onClick={() => navigate('/leave')}><Icon name="calendar" size={16} /> Review leave</button>}
          </div>
        </div>
        <div className="hero-panel">
          <div className="hero-kpis">
            <div className="hero-kpi"><div className="k">Today</div><div className="v">{money(stats.todayRevenue)}</div><TrendChip value={stats.todayTrend} label="vs yday" /></div>
            <div className="hero-kpi"><div className="k">This month</div><div className="v">{money(stats.monthRevenue)}</div><TrendChip value={stats.monthTrend} label="vs last" /></div>
            <div className="hero-kpi"><div className="k">Avg. order</div><div className="v">{money(stats.avgOrder)}</div><span className="trend flat">{stats.monthCount} orders</span></div>
          </div>
          <div>
            <Sparkline data={stats.days.map((d) => d.value)} width={480} height={70} />
            <div className="hero-spark-label"><span>{stats.days[0].label}</span><span>Sales, last 14 days</span><span>Today</span></div>
          </div>
        </div>
      </section>

      <div className="grid grid-4">
        <StatCard icon="receipt" tone="blue" label="Orders today" value={stats.todayCount} spark={stats.ordersPerDay} foot="Last 14 days" />
        <StatCard icon="users" tone="green" label="Customers" value={number(data.customers.length)} foot={`${stats.inactive.length} need a follow-up`} />
        <StatCard icon="box" tone="violet" label="Products" value={number(data.products.length)} foot={`Stock value ${money(stats.stockValue)}`} />
        <StatCard icon="alert" tone={stats.low.length ? 'amber' : 'green'} label="Low / out of stock" value={stats.low.length} foot={stats.low.length ? 'Needs attention' : 'All good'} />
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
                    <Avatar src={p.imageUrl} name={p.name} tint={categoryColor(p.category)} size={36} square />
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
      ) : (
        <div className="quick-actions">
          <Link to="/pos" className="quick-action"><div className="stat-icon tone-blue"><Icon name="cart" /></div><div><div className="qa-t">New sale</div><div className="qa-s">Ring up a customer's order</div></div></Link>
          <Link to="/products" className="quick-action"><div className="stat-icon tone-violet"><Icon name="box" /></div><div><div className="qa-t">Check stock</div><div className="qa-s">See what's available</div></div></Link>
          <Link to="/leave" className="quick-action"><div className="stat-icon tone-green"><Icon name="calendar" /></div><div><div className="qa-t">Request leave</div><div className="qa-s">{pendingLeave.length ? `${pendingLeave.length} pending` : 'Plan time off'}</div></div></Link>
          <Link to="/attendance" className="quick-action"><div className="stat-icon tone-cyan"><Icon name="clock" /></div><div><div className="qa-t">My shifts</div><div className="qa-s">Clock-in history</div></div></Link>
        </div>
      )}
    </div>
  );
}
