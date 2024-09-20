const newman = require('newman');
const path = require('path');

// Service to run the collection based on module and environment variables
exports.runCollection = (module, envVariables) => {
    return new Promise((resolve, reject) => {
        // Define the Postman collection file based on the module
        let collectionPath = path.resolve(__dirname, `../collections/${module}.postman_collection.json`);

        // Convert incoming envVariables object into Newman-friendly format
        const environmentVariables = Object.entries(envVariables).map(([key, value]) => {
            return { key, value };
        });

        // Run the Postman collection
        newman.run({
            collection: require(collectionPath),  // Path to Postman collection
            environment: { values: environmentVariables },  // Set environment variables
            reporters: 'cli'  // Optional CLI reporter for output
        })
        .on('request', function (err, args) {
            if (err) {
                console.error('Request Error:', err);
            } else {
                // Log request URL
                console.log(`Request: ${args.request.method} ${args.request.url.toString()}`);

                // Log the response status code and body
                const responseBody = args.response.stream.toString();
                console.log(`Response: ${args.response.statusCode}`);
                console.log(`Response Body: ${responseBody}`);
            }
        })
        .on('done', function (err, summary) {
            if (err) {
                return reject(err);
            }

            // Resolve with the summary once the collection run is completed
            resolve(summary);
        });
    });
};
