import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final TextEditingController _textController = TextEditingController();
  String _errorMessage = '';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Error Message Example',
      home: Scaffold(
        appBar: AppBar(
          title: Text('Error Message Example'),
        ),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                TextField(
                  controller: _textController,
                  decoration: InputDecoration(
                    labelText: 'Enter your name',
                    errorText: _errorMessage.isNotEmpty ? _errorMessage : null,
                    errorBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.red),
                    ),
                    focusedErrorBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.red),
                    ),
                  ),
                ),
                SizedBox(height: 20),
                ElevatedButton(
                  onPressed: () {
                    // Validate input and display error message if necessary
                    String input = _textController.text.trim();
                    if (input.isEmpty) {
                      setState(() {
                        _errorMessage = 'Name cannot be empty';
                      });
                    } else {
                      setState(() {
                        _errorMessage = '';
                      });
                      // Proceed with other actions
                    }
                  },
                  child: Text('Submit'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
