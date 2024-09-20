const express = require('express');
const bodyParser = require('body-parser');
const postmanRoutes = require('./routes/postmanRoutes');

const app = express();
const port = 3000;

// Middleware
app.use(bodyParser.json());  // For parsing JSON body

// Routes
app.use('/postman', postmanRoutes);

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
