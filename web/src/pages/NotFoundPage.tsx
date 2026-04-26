import React from 'react';
import { FiAlertCircle } from 'react-icons/fi';

const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-surface p-6">
      <div className="text-center max-w-sm w-full">
        <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
          <FiAlertCircle className="w-9 h-9 text-primary-500" />
        </div>

        <p className="text-5xl font-extrabold text-primary-200 mb-4 tracking-tight">404</p>
        <h1 className="text-xl font-bold text-on-surface mb-3">Esta página não existe</h1>

        <p className="text-on-surface-variant text-sm leading-relaxed">
          O link pode estar quebrado ou o álbum foi removido.
        </p>
      </div>
    </div>
  );
};

export default NotFoundPage;
