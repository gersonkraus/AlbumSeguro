// backend/src/models/AppUpdate.js
const mongoose = require('mongoose');

const appUpdateSchema = new mongoose.Schema(
  {
    versionCode: { type: Number, required: true },
    versionName: { type: String, required: true },
    apkFileName: { type: String, required: true },
    notes: { type: String, default: '' },
  },
  { timestamps: true }
);

module.exports = mongoose.model('AppUpdate', appUpdateSchema);
