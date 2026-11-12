import { Component } from 'react';

/*
 * ErrorBoundary.jsx
 * If a page crashes while drawing, show a message with a "Try again" button
 * instead of leaving the whole app as a blank white screen.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    console.error('Page crashed:', error, info?.componentStack);
  }

  componentDidUpdate(prev) {
    // moving to another page clears the error
    if (this.state.error && prev.resetKey !== this.props.resetKey) this.setState({ error: null });
  }

  render() {
    if (!this.state.error) return this.props.children;
    return (
      <div className="empty" style={{ padding: 40, textAlign: 'center' }}>
        <h3>Something went wrong on this page</h3>
        <p className="muted" style={{ margin: '8px 0 16px' }}>{String(this.state.error?.message || this.state.error)}</p>
        <button className="btn btn-primary" onClick={() => this.setState({ error: null })}>Try again</button>
      </div>
    );
  }
}
