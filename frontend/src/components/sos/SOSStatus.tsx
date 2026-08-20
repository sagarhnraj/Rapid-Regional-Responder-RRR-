import React, { useState } from 'react';
import { CheckCircle, X, MapPin, Clock } from 'lucide-react';
import { api } from '../../services/api';
import { SOSEvent } from '../../types';
import { InteractiveMap } from '../map/InteractiveMap';

interface SOSStatusProps {
  activeEvent: SOSEvent | null;
  onEventResolved: () => void;
}

export const SOSStatus: React.FC<SOSStatusProps> = ({ activeEvent, onEventResolved }) => {
  const [loading, setLoading] = useState(false);

  if (!activeEvent) return null;

  const resolveEvent = async () => {
    setLoading(true);
    try {
      await api.patch(`/sos/${activeEvent.id}/resolve`);
      onEventResolved();
    } catch (error) {
      console.error('Error resolving SOS event:', error);
    } finally {
      setLoading(false);
    }
  };

  const cancelEvent = async () => {
    setLoading(true);
    try {
      await api.patch(`/sos/${activeEvent.id}/cancel`);
      onEventResolved();
    } catch (error) {
      console.error('Error cancelling SOS event:', error);
    } finally {
      setLoading(false);
    }
  };

  const timeElapsed = Math.floor(
    (Date.now() - new Date(activeEvent.createdAt).getTime()) / 1000 / 60
  );

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm border border-red-200 space-y-6">
      <div className="text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 rounded-full mb-4">
          <div className="w-8 h-8 bg-red-500 rounded-full animate-pulse"></div>
        </div>
        <h2 className="text-xl font-semibold text-red-700">SOS Emergency Active</h2>
        <p className="text-gray-600 font-medium">{activeEvent.type} Emergency</p>
        <div className="flex items-center justify-center text-sm text-gray-500 mt-2">
          <Clock className="h-4 w-4 mr-1" />
          <span>{timeElapsed < 1 ? 'Just now' : `${timeElapsed} minutes ago`}</span>
        </div>
      </div>

      {/* Interactive Map View */}
      <div>
        <h3 className="text-sm font-semibold text-gray-700 mb-2 flex items-center">
          <MapPin className="h-4 w-4 mr-1 text-red-500" />
          Emergency Location & Map Route
        </h3>
        <InteractiveMap
          victimLat={activeEvent.latitude}
          victimLng={activeEvent.longitude}
          victimLabel={`${activeEvent.type} Emergency`}
          height="220px"
        />
      </div>

      {/* Responders Section */}
      {activeEvent.responders && activeEvent.responders.length > 0 ? (
        <div className="space-y-3">
          <h3 className="font-medium text-green-700 text-sm">
            ✓ {activeEvent.responders.length} Volunteer Responder{activeEvent.responders.length > 1 ? 's' : ''} En Route
          </h3>
          <div className="space-y-2">
            {activeEvent.responders.map((responder, idx) => (
              <div key={idx} className="p-3 bg-green-50 border border-green-200 rounded-lg flex justify-between items-center">
                <div>
                  <p className="font-medium text-green-900">{responder.volunteerName}</p>
                  <p className="text-xs text-green-700">ETA: {responder.estimatedArrival || 'En route'}</p>
                  {responder.message && <p className="text-xs text-gray-600 mt-1">"{responder.message}"</p>}
                </div>
                {responder.volunteerPhone && (
                  <a
                    href={`tel:${responder.volunteerPhone}`}
                    className="bg-green-600 hover:bg-green-700 text-white p-2 rounded-full text-xs font-semibold"
                  >
                    📞 Call
                  </a>
                )}
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-center text-xs text-amber-800">
          ⏳ Searching for available responders within 1km radius...
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex space-x-3 pt-2">
        <button
          onClick={resolveEvent}
          disabled={loading}
          className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white font-semibold py-3 px-4 rounded-lg transition-colors flex items-center justify-center"
        >
          <CheckCircle className="h-5 w-5 mr-2" />
          Mark Resolved
        </button>
        <button
          onClick={cancelEvent}
          disabled={loading}
          className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold py-3 px-4 rounded-lg transition-colors flex items-center justify-center"
        >
          <X className="h-5 w-5 mr-2" />
          Cancel Alert
        </button>
      </div>
    </div>
  );
};
