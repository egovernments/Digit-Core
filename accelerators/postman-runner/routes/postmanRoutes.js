const express = require('express');
const router = express.Router();
const postmanController = require('../controllers/postmanController');

// Define the route to trigger Postman collections based on the module
router.post('/run', postmanController.runPostmanCollection);

module.exports = router;
