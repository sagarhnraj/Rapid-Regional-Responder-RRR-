import React, { useState, useEffect } from 'react';
import { MapPin, Clock, Heart, Flame, Shield, HelpCircle, CheckCircle } from 'lucide-react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { SOSEvent, VolunteerProfile } from '../../types';
import { InteractiveMap } from '../map/InteractiveMap';

export const VolunteerDashboard: React.FC = () => {
  const [profile, setProfile] = useState<VolunteerProfile | null>(null);
  const [emergencies, setEmergencies] = useState<SOSEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedEmergency, setSelectedEmergency] = useState<SOSEvent | null>(null);
  const { user } = useAuth();

  useEffect(() => {
    fetchVolunteerProfile();
    fetchNearbyEmergencies();

    const interval = setInterval(fetchNearbyEmergencies, 5000);
    return () => clearInterval(interval);
  }, [user]);

  const fetchVolunteerProfile = async () => {
    try {
      const response = await api.get('/volunteers/profile');
      setProfile(response.data.data);
    } catch {
      setProfile(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchNearbyEmergencies = async () => {
    try {
      const response = await api.get('/volunteers/nearby');
      setEmergencies(response.data.data || []);
    } catch {
      setEmergencies([]);
    }
  };

  const toggleAvailability = async () => {
    try {
      const response = await api.patch('/volunteers/availability');
      setProfile(response.data.data);
    } catch (error) {
      console.error('Error toggling availability:', error);
    }
  };

  const respondToSOS = async (sosId: string) => {
    try {
      await api.post(`/sos/${sosId}/accept`, {
        estimatedArrival: '5 mins',
        message: 'Responder en route to location'
      });
      fetchNearbyEmergencies();
    } catch (error: any) {
      alert(error.message || 'Could not accept emergency. It may have already been claimed by another responder.');
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'Health': return <Heart className="h-5 w-5 text-red-500" />;
      case 'Fire': return <Flame className="h-5 w-5 text-orange-500" />;
      case 'Threat/Theft': return <Shield className="h-5 w-5 text-purple-500" />;
      default: return <HelpCircle className="h-5 w-5 text-gray-500" />;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Availability Status Box */}
      <div className="bg-white rounded-xl p-6 shadow-sm border">
        <h2 className="text-xl font-semibold mb-4">Volunteer Status</h2>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-gray-600">
              {profile?.isAvailable ? 'You are active & available to respond' : 'You are currently offline'}
            </p>
            {profile?.isAvailable && (
              <p className="text-sm text-green-600 mt-1 flex items-center">
                <CheckCircle className="h-4 w-4 mr-1" /> System Max Radius: 1 km
              </p>
            )}
          </div>
          <button
            onClick={toggleAvailability}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              profile?.isAvailable
                ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                : 'bg-green-600 text-white hover:bg-green-700'
            }`}
          >
            {profile?.isAvailable ? 'Go Offline' : 'Become Available'}
          </button>
        </div>
      </div>

      {/* Active Emergencies List */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold">Active Emergencies Near You (&lt; 1km)</h3>

        {emergencies.length === 0 ? (
          <div className="bg-white rounded-xl p-8 text-center shadow-sm border">
            <MapPin className="h-12 w-12 text-gray-400 mx-auto mb-2" />
            <p className="text-gray-500">No active emergencies in your area</p>
            <p className="text-xs text-gray-400 mt-1">We'll alert you instantly when help is needed nearby</p>
          </div>
        ) : (
          <div className="space-y-4">
            {emergencies.map((event) => (
              <div key={event.id} className="bg-white rounded-xl p-4 shadow-sm border space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    {getTypeIcon(event.type)}
                    <span className="font-semibold text-gray-900">{event.type} Emergency</span>
                  </div>
                  <span className="text-xs text-gray-500 flex items-center">
                    <Clock className="h-3 w-3 mr-1" />
                    {new Date(event.createdAt).toLocaleTimeString()}
                  </span>
                </div>

                {/* Leaflet Map for Emergency */}
                <InteractiveMap
                  victimLat={event.latitude}
                  victimLng={event.longitude}
                  volunteerLat={profile?.latitude}
                  volunteerLng={profile?.longitude}
                  victimLabel={`${event.type} Emergency`}
                  height="180px"
                />

                <div className="flex space-x-3">
                  <button
                    onClick={() => respondToSOS(event.id)}
                    className="flex-1 bg-green-600 hover:bg-green-700 text-white py-2.5 px-4 rounded-lg font-semibold transition-colors"
                  >
                    Accept & Respond
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
