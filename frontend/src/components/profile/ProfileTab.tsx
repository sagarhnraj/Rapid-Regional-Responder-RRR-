import React, { useState, useEffect } from 'react';
import { User as UserIcon, Phone, Heart, Plus, Edit2, Save, X, ShieldAlert, CheckCircle } from 'lucide-react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { EmergencyContact, UserProfile } from '../../types';

export const ProfileTab: React.FC = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [emergencyContacts, setEmergencyContacts] = useState<EmergencyContact[]>([]);
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [isAddingContact, setIsAddingContact] = useState(false);
  const [showVolunteerModal, setShowVolunteerModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [volunteerSkills, setVolunteerSkills] = useState<string[]>(['First Aid', 'CPR']);
  const { user, onboardVolunteer, logout } = useAuth();

  const [profileForm, setProfileForm] = useState({
    name: '',
    phone: '',
    medicalInfo: ''
  });

  const [contactForm, setContactForm] = useState({
    name: '',
    phone: '',
    relationship: 'Friend',
    isPrimary: false
  });

  useEffect(() => {
    if (user) {
      fetchProfile();
      fetchEmergencyContacts();
    }
  }, [user]);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/citizens/profile');
      setProfile(response.data.data);
      setProfileForm({
        name: response.data.data.name || '',
        phone: response.data.data.phone || '',
        medicalInfo: response.data.data.medicalInfo || ''
      });
    } catch (error) {
      console.error('Error fetching profile:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchEmergencyContacts = async () => {
    try {
      const response = await api.get('/citizens/contacts');
      setEmergencyContacts(response.data.data || []);
    } catch (error) {
      console.error('Error fetching contacts:', error);
    }
  };

  const saveProfile = async () => {
    try {
      const response = await api.put('/citizens/profile', profileForm);
      setProfile(response.data.data);
      setIsEditingProfile(false);
    } catch (error) {
      console.error('Error updating profile:', error);
    }
  };

  const saveContact = async () => {
    try {
      const response = await api.post('/citizens/contacts', contactForm);
      setEmergencyContacts(prev => [response.data.data, ...prev]);
      setIsAddingContact(false);
      setContactForm({ name: '', phone: '', relationship: 'Friend', isPrimary: false });
    } catch (error) {
      console.error('Error adding contact:', error);
    }
  };

  const deleteContact = async (contactId: string) => {
    try {
      await api.delete(`/citizens/contacts/${contactId}`);
      setEmergencyContacts(prev => prev.filter(c => c.id !== contactId));
    } catch (error) {
      console.error('Error deleting contact:', error);
    }
  };

  const handleVolunteerOnboarding = async () => {
    try {
      await onboardVolunteer(volunteerSkills, 1000);
      setShowVolunteerModal(false);
      alert('Volunteer onboarding complete! Volunteer privileges enabled.');
    } catch (error) {
      console.error('Error onboarding volunteer:', error);
    }
  };

  const toggleSkill = (skill: string) => {
    setVolunteerSkills(prev =>
      prev.includes(skill) ? prev.filter(s => s !== skill) : [...prev, skill]
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-20">
      {/* Profile Section */}
      <div className="bg-white rounded-xl p-6 shadow-sm border">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold flex items-center">
            <UserIcon className="h-5 w-5 mr-2 text-red-600" />
            Profile Information
          </h2>
          {!isEditingProfile ? (
            <button
              onClick={() => setIsEditingProfile(true)}
              className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full transition-colors"
            >
              <Edit2 className="h-4 w-4" />
            </button>
          ) : (
            <div className="flex space-x-2">
              <button
                onClick={saveProfile}
                className="p-2 text-green-600 hover:bg-green-50 rounded-full transition-colors"
              >
                <Save className="h-4 w-4" />
              </button>
              <button
                onClick={() => setIsEditingProfile(false)}
                className="p-2 text-gray-500 hover:bg-gray-100 rounded-full transition-colors"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
            {isEditingProfile ? (
              <input
                type="text"
                value={profileForm.name}
                onChange={(e) => setProfileForm(prev => ({ ...prev, name: e.target.value }))}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-red-500"
              />
            ) : (
              <p className="text-gray-900 font-medium">{profile?.name || 'Not set'}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Phone Number</label>
            {isEditingProfile ? (
              <input
                type="tel"
                value={profileForm.phone}
                onChange={(e) => setProfileForm(prev => ({ ...prev, phone: e.target.value }))}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-red-500"
              />
            ) : (
              <p className="text-gray-900 flex items-center">
                <Phone className="h-4 w-4 mr-2 text-gray-400" />
                {profile?.phone || 'Not set'}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Medical Information</label>
            {isEditingProfile ? (
              <textarea
                value={profileForm.medicalInfo}
                onChange={(e) => setProfileForm(prev => ({ ...prev, medicalInfo: e.target.value }))}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-red-500 h-24"
                placeholder="Allergies, blood group, medications..."
              />
            ) : (
              <p className="text-gray-900 flex items-center">
                <Heart className="h-4 w-4 mr-2 text-gray-400" />
                {profile?.medicalInfo || 'None specified'}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Become a Volunteer Banner / Onboarding Section */}
      {user?.role !== 'VOLUNTEER' ? (
        <div className="bg-gradient-to-r from-red-600 to-orange-600 rounded-xl p-6 text-white shadow-md space-y-3">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="h-6 w-6" />
            <h3 className="text-lg font-bold">Become a Volunteer Responder</h3>
          </div>
          <p className="text-sm opacity-90 leading-relaxed">
            Help citizens in emergency situations within your 1 km radius. Join trained community responders in saving lives.
          </p>
          <button
            onClick={() => setShowVolunteerModal(true)}
            className="w-full bg-white text-red-600 hover:bg-red-50 font-bold py-3 rounded-lg shadow transition-colors"
          >
            Become a Volunteer
          </button>
        </div>
      ) : (
        <div className="bg-green-50 border border-green-200 rounded-xl p-4 flex items-center space-x-3 text-green-800">
          <CheckCircle className="h-6 w-6 text-green-600" />
          <div>
            <p className="font-semibold">Volunteer Responder Active</p>
            <p className="text-xs text-green-700">You have volunteer capabilities enabled on your RRR account.</p>
          </div>
        </div>
      )}

      {/* Emergency Contacts Section */}
      <div className="bg-white rounded-xl p-6 shadow-sm border">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Emergency Contacts</h2>
          <button
            onClick={() => setIsAddingContact(true)}
            className="flex items-center space-x-1 bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-lg font-medium text-sm"
          >
            <Plus className="h-4 w-4" />
            <span>Add Contact</span>
          </button>
        </div>

        {emergencyContacts.length === 0 ? (
          <p className="text-sm text-gray-500 text-center py-4">No emergency contacts added yet</p>
        ) : (
          <div className="space-y-3">
            {emergencyContacts.map((contact) => (
              <div key={contact.id} className="p-4 border rounded-lg flex justify-between items-center">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-semibold text-gray-900">{contact.name}</span>
                    {contact.isPrimary && (
                      <span className="bg-red-100 text-red-700 text-xs px-2 py-0.5 rounded-full font-medium">Primary</span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600">{contact.phone} • {contact.relationship}</p>
                </div>
                <button
                  onClick={() => deleteContact(contact.id)}
                  className="p-2 text-red-500 hover:bg-red-50 rounded-full"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Volunteer Onboarding Modal */}
      {showVolunteerModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4 shadow-2xl">
            <h3 className="text-lg font-bold text-gray-900">Volunteer Onboarding</h3>
            <p className="text-xs text-gray-600">Select your emergency skills to complete your volunteer registration:</p>

            <div className="space-y-2">
              {['First Aid', 'CPR', 'Fire Fighting', 'Emergency Navigation', 'Search & Rescue'].map((skill) => (
                <label key={skill} className="flex items-center space-x-2 p-2 border rounded-lg cursor-pointer hover:bg-gray-50">
                  <input
                    type="checkbox"
                    checked={volunteerSkills.includes(skill)}
                    onChange={() => toggleSkill(skill)}
                    className="rounded text-red-600 focus:ring-red-500"
                  />
                  <span className="text-sm font-medium text-gray-800">{skill}</span>
                </label>
              ))}
            </div>

            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800">
              ℹ️ Maximum emergency response notification radius is capped at 1 km by system policy.
            </div>

            <div className="flex space-x-3 pt-2">
              <button
                onClick={handleVolunteerOnboarding}
                className="flex-1 bg-red-600 hover:bg-red-700 text-white py-2.5 rounded-lg font-semibold"
              >
                Complete Onboarding
              </button>
              <button
                onClick={() => setShowVolunteerModal(false)}
                className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 py-2.5 rounded-lg font-semibold"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Contact Modal */}
      {isAddingContact && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl w-full max-w-md p-6 space-y-4 shadow-2xl">
            <h3 className="text-lg font-semibold">Add Emergency Contact</h3>
            <input
              type="text"
              placeholder="Name"
              value={contactForm.name}
              onChange={(e) => setContactForm(prev => ({ ...prev, name: e.target.value }))}
              className="w-full p-3 border rounded-lg"
            />
            <input
              type="tel"
              placeholder="Phone"
              value={contactForm.phone}
              onChange={(e) => setContactForm(prev => ({ ...prev, phone: e.target.value }))}
              className="w-full p-3 border rounded-lg"
            />
            <select
              value={contactForm.relationship}
              onChange={(e) => setContactForm(prev => ({ ...prev, relationship: e.target.value }))}
              className="w-full p-3 border rounded-lg"
            >
              <option value="Family">Family</option>
              <option value="Friend">Friend</option>
              <option value="Spouse">Spouse</option>
              <option value="Doctor">Doctor</option>
            </select>
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                id="primary"
                checked={contactForm.isPrimary}
                onChange={(e) => setContactForm(prev => ({ ...prev, isPrimary: e.target.checked }))}
              />
              <label htmlFor="primary" className="text-sm">Set as primary emergency contact</label>
            </div>
            <div className="flex space-x-3 pt-2">
              <button onClick={saveContact} className="flex-1 bg-red-600 text-white py-2.5 rounded-lg font-semibold">Save</button>
              <button onClick={() => setIsAddingContact(false)} className="flex-1 bg-gray-200 text-gray-700 py-2.5 rounded-lg font-semibold">Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Logout */}
      <button onClick={logout} className="w-full bg-gray-200 text-gray-700 py-3 rounded-lg font-semibold">Sign Out</button>
    </div>
  );
};