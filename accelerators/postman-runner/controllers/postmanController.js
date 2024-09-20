const postmanService = require('../services/postmanService');

// Controller to run the Postman collection
exports.runPostmanCollection = async (req, res) => {
    try {
        const { module, envVariables } = req.body;

        if (!module || !envVariables) {
            return res.status(400).json({ error: 'Module and envVariables are required' });
        }

        // Pass module and env variables to the service
        const result = await postmanService.runCollection(module, envVariables);
        res.status(200).json({ message: 'Collection executed successfully', result });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
