import React from 'react';

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div role="alert" className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
          <div className="card max-w-md text-center">
            <h1 className="text-lg font-semibold text-slate-800">Something went wrong</h1>
            <p className="mt-2 text-sm text-slate-600">
              The page could not be displayed. Please refresh and try again.
            </p>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
