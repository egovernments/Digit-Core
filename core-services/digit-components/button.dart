import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Button Types Example',
      home: Scaffold(
        appBar: AppBar(
          title: Text('Button Types Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              DefaultButton(text: 'Save and Continue'),
              SizedBox(height: 10),
              SecondaryButton(text: 'Cancel'),
              SizedBox(height: 10),
              MultiButton(text: 'Download', subActions: ['PDF', 'Excel', 'CSV']),
              SizedBox(height: 10),
              DisableButton(text: 'Disabled', onPressed: null),
            ],
          ),
        ),
      ),
    );
  }
}

class DefaultButton extends StatelessWidget {
  final String text;

  DefaultButton({required this.text});

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () {},
      child: Text(text),
    );
  }
}

class SecondaryButton extends StatelessWidget {
  final String text;

  SecondaryButton({required this.text});

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: () {},
      child: Text(text),
    );
  }
}

class MultiButton extends StatelessWidget {
  final String text;
  final List<String> subActions;

  MultiButton({required this.text, required this.subActions});

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton(
      itemBuilder: (BuildContext context) {
        return subActions.map((String action) {
          return PopupMenuItem(
            child: Text(action),
            value: action,
          );
        }).toList();
      },
      child: ElevatedButton(
        onPressed: () {},
        child: Text(text),
      ),
    );
  }
}

class DisableButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;

  DisableButton({required this.text, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      child: Text(text),
      style: ButtonStyle(
        backgroundColor: MaterialStateProperty.all<Color>(Colors.grey),
        textStyle: MaterialStateProperty.all<TextStyle>(TextStyle(color: Colors.white)),
      ),
    );
  }
}
