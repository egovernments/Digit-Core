// First,add the fluttertoast package to your pubspec.yaml file:

// dependencies:
//   flutter:
//     sdk: flutter
//   fluttertoast: ^8.0.8


import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Toast Example',
      home: Scaffold(
        appBar: AppBar(
          title: Text('Toast Example'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: () {
              // Display success toast
              showToast('Action completed successfully', Colors.green, Icons.check);
            },
            child: Text('Show Toast'),
          ),
        ),
      ),
    );
  }

  void showToast(String message, Color backgroundColor, IconData icon) {
    Fluttertoast.showToast(
      msg: message,
      toastLength: Toast.LENGTH_SHORT,
      gravity: ToastGravity.BOTTOM,
      backgroundColor: backgroundColor,
      textColor: Colors.white,
      fontSize: 16.0,
      timeInSecForIosWeb: 1,
      webShowClose: true,
      webPosition: 'center',
      webBgColor: '#e74c3c',
      // Set an icon before the text
      child: Row(
        children: [
          Icon(icon),
          SizedBox(width: 8),
          Text(message),
        ],
      ),
    );
  }
}
