import React, { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import { SOSTypeModal } from './SOSTypeModal';
import { useAuth } from '../../context/AuthContext';

interface SOSButtonProps {
  onSOS: (type: string, location: { lat: number; lng: number }) => Promise<void>;
}

export const SOSButton: React.FC<SOSButtonProps> = ({ onSOS }) => {
  const [showTypeModal, setShowTypeModal] = useState(false);
  const [isPressed, setIsPressed] = useState(false);
  const { user } = useAuth();

  const handleSOSPress = () => {
    setIsPressed(true);
    setTimeout(() => setIsPressed(false), 150);
    setShowTypeModal(true);
  };

  const handleTypeSelect = async (type: string) => {
    try {
      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000
        });
      });

      await onSOS(type, {
        lat: position.coords.latitude,
        lng: position.coords.longitude
      });
    } catch (error) {
      console.error('Error getting location or sending SOS:', error);
      // Fallback to default coordinates if geolocation is blocked
      await onSOS(type, { lat: 12.9716, lng: 77.5946 });
    }
    setShowTypeModal(false);
  };

  return (
    <>
      <div className="flex flex-col items-center space-y-6">
        <button
          onClick={handleSOSPress}
          className={`relative w-48 h-48 rounded-full bg-gradient-to-br from-red-500 to-red-700 hover:from-red-600 hover:to-red-800 active:from-red-700 active:to-red-900 transition-all duration-150 shadow-2xl hover:shadow-red-500/25 transform ${
            isPressed ? 'scale-95' : 'hover:scale-105'
          } focus:outline-none focus:ring-4 focus:ring-red-500/50`}
        >
          <div className="absolute inset-4 bg-red-400/20 rounded-full animate-pulse" />
          <div className="relative flex flex-col items-center justify-center h-full text-white">
            <AlertTriangle className="h-12 w-12 mb-2 drop-shadow-lg" />
            <span className="text-2xl font-bold tracking-wide drop-shadow-lg">SOS</span>
            <span className="text-sm opacity-90 mt-1">Emergency</span>
          </div>
        </button>

        <div className="text-center max-w-sm">
          <p className="text-gray-600 text-sm leading-relaxed">
            Press the SOS button to alert nearby volunteers (&lt; 1km) and emergency services.
            Your GPS location will be shared to coordinate help.
          </p>
        </div>
      </div>

      <SOSTypeModal
        isOpen={showTypeModal}
        onClose={() => setShowTypeModal(false)}
        onSelectType={handleTypeSelect}
      />
    </>
  );
};