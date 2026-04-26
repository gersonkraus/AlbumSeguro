const mongoose = require('mongoose');

const appConfigSchema = new mongoose.Schema({
  childAlbumBaseUrl: {
    type: String,
    default: '',
    trim: true,
  },
}, { timestamps: true });

module.exports = mongoose.model('AppConfig', appConfigSchema);
