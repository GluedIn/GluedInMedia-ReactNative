import UIKit
import React
import React_RCTAppDelegate
import ReactAppDependencyProvider

@main
class AppDelegate: RCTAppDelegate {
  var reactNativeViewController: UINavigationController?
  var reactNativeNavigationController: UINavigationController?

  
  func currentRootViewController() -> UINavigationController? {
      return reactNativeViewController
  }
  override func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
      
      self.moduleName = "GluedInMedia"
      self.dependencyProvider = RCTAppDependencyProvider()
      
//      DispatchQueue.main.async {
//          self.setupReactNativeApp()
//      }
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
//      return true
  }

  private func setupReactNativeApp() {
      // ✅ Ensure React Native bridge is initialized before setting the rootViewController
      let bridge = RCTBridge(delegate: self, launchOptions: nil)

      let reactNativeVC = ReactNativeViewController()
      reactNativeNavigationController = UINavigationController(rootViewController: reactNativeVC)
      
      self.window = UIWindow(frame: UIScreen.main.bounds)
      self.window.rootViewController = reactNativeNavigationController
      self.window.makeKeyAndVisible()
  }

  // ✅ Method to Access Navigation Controller from Anywhere
  func getNavigationController() -> UINavigationController? {
      return reactNativeNavigationController
  }
  
  override func sourceURL(for bridge: RCTBridge) -> URL? {
    self.bundleURL()
  }

  override func bundleURL() -> URL? {
#if DEBUG
    RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index")
#else
    Bundle.main.url(forResource: "main", withExtension: "jsbundle")
#endif
  }
}
