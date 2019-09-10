import 'package:flutter/material.dart';
import 'package:flutter_boost/flutter_boost.dart';
import 'dart:ui';
import 'simple_page_widgets.dart';
void main() {
//  runApp(_widgetForRoute(window.defaultRouteName));
  runApp(MyApp());
}



class App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text("main",textDirection: TextDirection.ltr, style: TextStyle(fontSize: 24, color: Colors.red),),);
  }
}



class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();

    FlutterBoost.singleton.registerPageBuilders({
      'first': (pageName, params, _) => FirstRouteWidget(),
      'second': (pageName, params, _) => SecondRouteWidget(params),
      'tab': (pageName, params, _) => TabRouteWidget(),
      'flutterFragment': (pageName, params, _) => FragmentRouteWidget(params),

      ///可以在native层通过 getContainerParams 来传递参数
      'flutterPage': (pageName, params, _) {
        print("flutterPage params:$params");

        return FlutterRouteWidget();
      },
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Flutter Boost example',
        builder: FlutterBoost.init(postPush: _onRoutePushed),
        home: Container());
  }

  void _onRoutePushed(
      String pageName, String uniqueId, Map params, Route route, Future _) {
  }
}

