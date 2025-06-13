//
//  ReactNativeViewController.swift
//  test
//
//  Created by Amit Choudhary on 10/03/25.
//

import Foundation
import UIKit
import React

class ReactNativeViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        
        DispatchQueue.main.async {
            self.setupReactNativeView()
        }
    }
    
    private func setupReactNativeView() {
        guard let jsCodeLocation = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index") else {
            print("❌ Error: Could not load JS bundle. Make sure Metro Bundler is running.")
            return
        }

        let rootView = RCTRootView(
            bundleURL: jsCodeLocation,
            moduleName: "test",  // ✅ Make sure this matches your React Native entry file
            initialProperties: nil,
            launchOptions: nil
        )

        rootView.frame = self.view.bounds
        rootView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        self.view.addSubview(rootView)
    }
}
