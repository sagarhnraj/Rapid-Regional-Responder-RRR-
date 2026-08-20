import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet marker icon asset paths
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface InteractiveMapProps {
  victimLat: number;
  victimLng: number;
  volunteerLat?: number;
  volunteerLng?: number;
  victimLabel?: string;
  volunteerLabel?: string;
  height?: string;
}

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  victimLat,
  victimLng,
  volunteerLat,
  volunteerLng,
  victimLabel = 'Emergency SOS Location',
  volunteerLabel = 'Responding Volunteer',
  height = '300px',
}) => {
  const center: [number, number] = [victimLat, victimLng];

  // Calculate distance in meters
  const calculateDistanceMeters = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
    const R = 6371000;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(R * c);
  };

  const hasVolunteer = volunteerLat !== undefined && volunteerLng !== undefined;
  const distance = hasVolunteer ? calculateDistanceMeters(victimLat, victimLng, volunteerLat!, volunteerLng!) : null;

  return (
    <div className="w-full rounded-xl overflow-hidden shadow-md border border-gray-200 relative z-0">
      <MapContainer center={center} zoom={15} style={{ height, width: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Victim Emergency Marker */}
        <Marker position={[victimLat, victimLng]}>
          <Popup>
            <div className="font-semibold text-red-600">🚨 {victimLabel}</div>
            <div className="text-xs text-gray-600">
              {victimLat.toFixed(4)}, {victimLng.toFixed(4)}
            </div>
          </Popup>
        </Marker>

        {/* Volunteer Marker */}
        {hasVolunteer && (
          <Marker position={[volunteerLat!, volunteerLng!]}>
            <Popup>
              <div className="font-semibold text-blue-600">🚑 {volunteerLabel}</div>
              {distance && <div className="text-xs text-gray-600">Distance: {distance} meters away</div>}
            </Popup>
          </Marker>
        )}

        {/* Direct Route Polyline */}
        {hasVolunteer && (
          <Polyline
            positions={[
              [victimLat, victimLng],
              [volunteerLat!, volunteerLng!],
            ]}
            color="#ef4444"
            weight={4}
            dashArray="8, 8"
          />
        )}
      </MapContainer>

      {hasVolunteer && distance !== null && (
        <div className="bg-white/95 backdrop-blur-sm px-3 py-1.5 rounded-lg shadow-sm border text-xs font-semibold text-gray-800 absolute bottom-3 left-3 z-[1000]">
          📍 Responding Distance: {distance > 1000 ? `${(distance / 1000).toFixed(2)} km` : `${distance} meters`}
        </div>
      )}
    </div>
  );
};
