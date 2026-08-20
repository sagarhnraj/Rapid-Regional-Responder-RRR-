import React, { useState } from 'react';
import { X, User, Mail, Lock, Phone, AlertTriangle, CheckCircle, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose }) => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Registration Flow State: Google Identity Verification
  const [registrationStep, setRegistrationStep] = useState<1 | 2>(1);
  const [googleIdToken, setGoogleIdToken] = useState('');

  const { login, verifyGoogleRegistration, register } = useAuth();

  if (!isOpen) return null;

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await login(email, password);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Login failed. Invalid RRR Email or Password.');
    } finally {
      setLoading(false);
    }
  };

  const handleStartGoogleVerification = async () => {
    setError('');
    // Prompt user for Google OAuth verification ID Token (or fallback test token)
    const tokenInput = window.prompt("Registration Identity Verification: Enter Google OAuth ID Token (or click OK for test verification):");
    const idToken = tokenInput && tokenInput.trim() !== '' ? tokenInput.trim() : 'mock_google_id_token_demo';

    setLoading(true);
    try {
      const googleIdentity = await verifyGoogleRegistration(idToken);
      setGoogleIdToken(idToken);
      setEmail(googleIdentity.email);
      setName(googleIdentity.name || '');
      setRegistrationStep(2); // Proceed to Step 2: Complete RRR details
    } catch (err: any) {
      setError(err.message || 'Google identity verification failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      setLoading(false);
      return;
    }
    if (!phone || phone.trim() === '') {
      setError('Phone number is required');
      setLoading(false);
      return;
    }

    try {
      await register(googleIdToken, email, password, name, phone);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please check your information.');
    } finally {
      setLoading(false);
    }
  };

  const resetState = (toSignUp: boolean) => {
    setIsSignUp(toSignUp);
    setRegistrationStep(1);
    setGoogleIdToken('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setName('');
    setPhone('');
    setError('');
  };

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl w-full max-w-md shadow-2xl">
        <div className="flex justify-between items-center p-6 border-b">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="h-6 w-6 text-red-600" />
            <h2 className="text-xl font-bold text-gray-900">
              {isSignUp ? 'Register for RRR' : 'Login to RRR'}
            </h2>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X className="h-5 w-5" />
          </button>
        </div>

        {error && (
          <div className="mx-6 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
            {error}
          </div>
        )}

        {!isSignUp ? (
          /* ==================== LOGIN SCREEN (EMAIL + PASSWORD ONLY) ==================== */
          <form onSubmit={handleLoginSubmit} className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                  placeholder="Enter your email"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                  placeholder="Enter your password"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg transition-colors disabled:opacity-50"
            >
              {loading ? 'Authenticating...' : 'Login'}
            </button>

            <p className="text-center text-sm text-gray-600 pt-2">
              Don't have an account?{' '}
              <button
                type="button"
                onClick={() => resetState(true)}
                className="text-red-600 hover:text-red-700 font-semibold"
              >
                Register
              </button>
            </p>
          </form>
        ) : (
          /* ==================== REGISTRATION FLOW (GOOGLE IDENTITY VERIFICATION FIRST) ==================== */
          <div className="p-6 space-y-4">
            {registrationStep === 1 ? (
              /* STEP 1: GOOGLE OAUTH VERIFICATION */
              <div className="space-y-4 text-center">
                <div className="p-4 bg-red-50 rounded-xl border border-red-100">
                  <ShieldCheck className="h-10 w-10 text-red-600 mx-auto mb-2" />
                  <h3 className="font-semibold text-gray-900">Step 1: Verify Google Identity</h3>
                  <p className="text-xs text-gray-600 mt-1 leading-relaxed">
                    To prevent fraudulent emergency accounts, RRR requires Google OAuth identity verification before account creation.
                  </p>
                </div>

                <button
                  type="button"
                  onClick={handleStartGoogleVerification}
                  disabled={loading}
                  className="w-full border border-gray-300 hover:bg-gray-50 text-gray-800 font-semibold py-3.5 rounded-lg flex items-center justify-center space-x-3 transition-colors shadow-sm"
                >
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                  </svg>
                  <span>{loading ? 'Verifying...' : 'Verify Identity with Google'}</span>
                </button>

                <p className="text-center text-sm text-gray-600 pt-2">
                  Already have an RRR account?{' '}
                  <button
                    type="button"
                    onClick={() => resetState(false)}
                    className="text-red-600 hover:text-red-700 font-semibold"
                  >
                    Login
                  </button>
                </p>
              </div>
            ) : (
              /* STEP 2: COLLECT REQUIRED RRR DETAILS & PASSWORD */
              <form onSubmit={handleRegisterSubmit} className="space-y-4">
                <div className="p-3 bg-green-50 border border-green-200 rounded-lg flex items-center space-x-2 text-xs text-green-800">
                  <CheckCircle className="h-4 w-4 text-green-600 flex-shrink-0" />
                  <span>Google Identity Verified: <strong>{email}</strong></span>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                      placeholder="Full Name"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Phone Number</label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                      placeholder="Phone Number"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Create RRR Password</label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                      placeholder="Password (min 6 chars)"
                      minLength={6}
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Confirm RRR Password</label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
                      placeholder="Confirm Password"
                      minLength={6}
                      required
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg transition-colors disabled:opacity-50"
                >
                  {loading ? 'Creating Account...' : 'Create RRR Account'}
                </button>
              </form>
            )}
          </div>
        )}
      </div>
    </div>
  );
};