import React, { useState, useEffect } from 'react';
import { Clock, MapPin, Heart, Flame, Shield, HelpCircle, CheckCircle, X } from 'lucide-react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { SOSEvent } from '../../types';

export const HistoryLog: React.FC = () => {
  const [events, setEvents] = useState<SOSEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    fetchHistory();
  }, [user]);

  const fetchHistory = async () => {
    try {
      const response = await api.get('/sos/my');
      setEvents(response.data.data || []);
    } catch (error) {
      console.error('Error fetching history:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
      case 'NOTIFYING':
        return <span className="bg-red-100 text-red-700 px-2 py-0.5 rounded-full text-xs font-semibold">Active</span>;
      case 'ACCEPTED':
      case 'RESPONDING':
      case 'ARRIVED':
        return <span className="bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full text-xs font-semibold">Responding</span>;
      case 'RESOLVED':
        return <span className="bg-green-100 text-green-700 px-2 py-0.5 rounded-full text-xs font-semibold">Resolved</span>;
      case 'CANCELLED':
        return <span className="bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full text-xs font-semibold">Cancelled</span>;
      default:
        return <span className="bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full text-xs font-semibold">{status}</span>;
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
      <div className="text-center">
        <h2 className="text-xl font-semibold mb-1">Emergency History</h2>
        <p className="text-sm text-gray-600">Your past SOS events and responses</p>
      </div>

      {events.length === 0 ? (
        <div className="bg-white rounded-xl p-8 text-center shadow-sm border">
          <Clock className="h-12 w-12 text-gray-400 mx-auto mb-2" />
          <p className="text-gray-500">No emergency events recorded</p>
        </div>
      ) : (
        <div className="space-y-3">
          {events.map((event) => (
            <div key={event.id} className="bg-white rounded-xl p-4 shadow-sm border space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  {getTypeIcon(event.type)}
                  <span className="font-semibold text-gray-900">{event.type} Emergency</span>
                </div>
                {getStatusBadge(event.status)}
              </div>

              <div className="text-xs text-gray-600 space-y-1">
                <div className="flex items-center">
                  <Clock className="h-3.5 w-3.5 mr-1 text-gray-400" />
                  <span>{new Date(event.createdAt).toLocaleString()}</span>
                </div>
                <div className="flex items-center">
                  <MapPin className="h-3.5 w-3.5 mr-1 text-gray-400" />
                  <span>{event.address || `${event.latitude.toFixed(4)}, ${event.longitude.toFixed(4)}`}</span>
                </div>
              </div>

              {event.resolvedAt && (
                <div className="text-xs text-green-700 font-medium pt-1 flex items-center">
                  <CheckCircle className="h-3.5 w-3.5 mr-1" />
                  Resolved at {new Date(event.resolvedAt).toLocaleTimeString()}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};