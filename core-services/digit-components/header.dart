// This Flutter code creates a basic header with a blue background color, white text, a logo placeholder (labeled as 
//'YourLogo'), and navigation links ('Home', 'About', 'Services', 'Contact'). You can further customize it according
// to your design requirements.


//general header format
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Header Design',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue,
        title: Row(
          children: [
            Text(
              'YourLogo',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            Spacer(),
            NavLinks(),
          ],
        ),
      ),
      body: Center(
        child: Text(
          'Your Content Here',
          style: TextStyle(fontSize: 20),
        ),
      ),
    );
  }
}

class NavLinks extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        NavItem(text: 'Home'),
        NavItem(text: 'About'),
        NavItem(text: 'Services'),
        NavItem(text: 'Contact'),
      ],
    );
  }
}

class NavItem extends StatelessWidget {
  final String text;

  NavItem({required this.text});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 18,
          color: Colors.white,
        ),
      ),
    );
  }
}


