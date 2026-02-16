//
//  GluedInGlobal.swift
//  GluedIn
//
//  Created by Amit Choudhary on 24/07/24.
//

import Foundation
internal import GluedInCoreSDK
//
//  GlobalManager.swift
//  SVC
//
//  Created by apple on 19/10/20.
//  Copyright © 2020 SAW. All rights reserved.
//

import UIKit
internal import GluedInCreatorSDK
//import FirebaseCore

class GluedInGlobal: NSObject {
    static let manager = GluedInGlobal()
    
    private override init() {
        super.init()
    }
    
//    func setIsCreator(status: Bool?) -> () {
//        let isCreator = status ?? true
//        UserDefaults.standard.set(isCreator, forKey: UserDefaultKeys.kCreator.rawValue)
//    }

    func getUserDetails() -> UserData? {
        return Account.sharedInstance.getUserDetails()
    }

    func getDeviceID() -> String {
        return  UIDevice.current.identifierForVendor!.uuidString.replacingOccurrences(of: "-", with: "")
    }
    
    
    //MARK: - isLoginStatus
    func isCheckUserLogin() -> Bool{
        return Account.sharedInstance.isUserLoggedIn()
    }
    
    func isSocialLogin() -> Bool{
        return Account.sharedInstance.getLoginType()
    }
     
    
    // MARK: - Method to render image from bundle
    
    func localImage(_ name: String, template: Bool = false) -> UIImage {
        var image = UIImage(named: name,
                            in: Bundle(for: type(of: self)),
                            compatibleWith: nil) ?? UIImage()
        if template {
            image = image.withRenderingMode(.alwaysTemplate)
        }
        return image
    }
    
    func getBundleId() -> String {
        return Bundle.main.bundleIdentifier ?? ""
    }
    
    func getAppName() -> String {
        return Bundle.main.appDisplayName ?? "App"
    }
    
    func getPlatform() -> String {
        return "iOS"
    }
    
    func getDeviceModel() -> String {
        return UIDevice.current.model
    }
        
    func setBackButtonImage() -> UIImage{
        return UIImage(named: "Back-Button-Vertical",
                       in: Bundle(for: type(of: self)),
                       compatibleWith: nil) ?? UIImage()
    }

    func getAppStoreId() -> String {
        return Account.sharedInstance.getPlistKeyValue(key: "APP_STORE_ID", defaultValue: "")
    }
    func getShareActivityBaseUrl() -> String {
        return Account.sharedInstance.getPlistKeyValue(key: "ASSOCIATED_DOMAIN", defaultValue: "")
    }
    
    func getGoogleClientId() -> String {
        guard let path = Bundle.main.path(forResource: "Info", ofType: "plist"),
              let nsDictionary = NSDictionary(contentsOfFile: path),
              let gluedinConfiguration = nsDictionary["GluedInConfiguration"] as? [String: Any],
              let clientId = gluedinConfiguration["CLIENT_ID"] as? String else {
            return ""
        }
        return clientId
    }
    
    func isAuthSkip() -> Bool{
        return Account.sharedInstance.getIsAuthSkipFromInfoPlist()
    }
    
    func isBottomBarEnable() -> Bool{
        return Account.sharedInstance.getIsBottomBarEnableFromInfoPlist()
    }
    
    func getStatusBarHeight() -> CGFloat {
        var statusBarHeight: CGFloat = 0
        if #available(iOS 13.0, *) {
            let window = UIApplication.shared.windows.filter {$0.isKeyWindow}.first
            statusBarHeight = window?.windowScene?.statusBarManager?.statusBarFrame.height ?? 0
        } else {
            statusBarHeight = UIApplication.shared.statusBarFrame.height
        }
        return statusBarHeight
    }
    
    
    func setBlackBackButtonImage() -> UIImage{
        return UIImage(named: "black_new_arrow",
                       in: Bundle(for: type(of: self)),
                       compatibleWith: nil) ?? UIImage()
    }
    
    func onSuccessTest(){}
    
    /// Method for Get Localised Value
    /// - Returns: Return type String
    func getLocalisedValue(
        localisedKey: String,
        defaultString: String
    ) -> String {
        let langString = getLocalisedLanguageKey()
        let path = Bundle.main.path(forResource: langString, ofType: "lproj") ?? ""
        let bundle = Bundle(path: path)
        let value = NSLocalizedString(
            localisedKey,
            tableName: nil,
            bundle: bundle ?? Bundle.main,
            value: defaultString,
            comment: "")
        return value
    }
    
    /// Method for Get language Value
    /// - Returns: String value as language
    func getLocalisedLanguageKey() -> String {
        let lang = Account.sharedInstance.getLocalisedLanguage()
        return lang ?? "en"
    }
    
    func getParentAppEnable() -> Bool {
        return Account.sharedInstance.getParentAppEnable()
    }
    
    func getSDKVersion() -> String? {
        return Bundle(for: GluedInGlobal.self).infoDictionary?["CFBundleShortVersionString"] as? String
    }
 
    func getRegion() -> String? {
        return Account.sharedInstance.getAccountData()?.country
    }
    
    /// Formats a raw price string into a clean display format with proper symbol.
    /// Supports inputs like: "SGD 5", "USD 12.50", "INR 199", "$ 5", "€9.99".
    func formatPrice(from raw: String?) -> String? {
        guard let raw = raw, !raw.isEmpty else { return nil }

        let (codeOrSymbol, amountString) = parseCurrencyAndAmount(from: raw)
        guard let amountString = amountString else { return nil }

        let symbol: String
        if let code = codeOrSymbol, code.count == 3 { // currency code like SGD, USD, INR
            symbol = symbolForCurrencyCode(code)
        } else {
            // Already a symbol like $, €, ₹, £ — keep as-is
            symbol = codeOrSymbol ?? ""
        }

        let cleanedAmount = normalizeAmountString(amountString)
        return symbol.isEmpty ? cleanedAmount : "\(symbol) \(cleanedAmount)"
    }

    /// Extracts a currency code or symbol and the numeric amount from a raw price string.
    private func parseCurrencyAndAmount(from raw: String) -> (codeOrSymbol: String?, amount: String?) {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)

        // Case 1: Starts with a 3-letter currency code (e.g., SGD 5, USD12.5)
        if let match = trimmed.range(of: "^[A-Za-z]{3}", options: .regularExpression) {
            let code = String(trimmed[match]).uppercased()
            if let numRange = trimmed.range(of: "[0-9]+(?:[.,][0-9]{1,2})?", options: .regularExpression) {
                let num = String(trimmed[numRange])
                return (code, num)
            }
            return (code, nil)
        }

        // Case 2: Starts with a common symbol ($, €, ₹, £)
        if let symRange = trimmed.range(of: "^[\u{0024}\u{20AC}\u{20B9}\u{00A3}]", options: .regularExpression) {
            let sym = String(trimmed[symRange])
            if let numRange = trimmed.range(of: "[0-9]+(?:[.,][0-9]{1,2})?", options: .regularExpression) {
                let num = String(trimmed[numRange])
                return (sym, num)
            }
            return (sym, nil)
        }

        // Fallback: find any number
        if let numRange = trimmed.range(of: "[0-9]+(?:[.,][0-9]{1,2})?", options: .regularExpression) {
            let num = String(trimmed[numRange])
            return (nil, num)
        }

        return (nil, nil)
    }

    /// Maps ISO 4217 code to a currency symbol. Includes SGD → S$ special handling.
    private func symbolForCurrencyCode(_ code: String) -> String {
        let map: [String: String] = [
            "USD": "$", "CAD": "$", "AUD": "$", "NZD": "$", "HKD": "$",
            "SGD": "S$", // Singapore Dollar special case
            "EUR": "€", "INR": "₹", "GBP": "£", "JPY": "¥", "CNY": "¥", "KRW": "₩",
            "AED": "د.إ", "SAR": "﷼", "RUB": "₽", "TRY": "₺", "THB": "฿"
        ]
        if let s = map[code] { return s }

        // Fallback to NumberFormatter for uncommon codes
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.currencyCode = code
        f.locale = Locale(identifier: "en_US_POSIX")
        return f.currencySymbol ?? code
    }

    /// Formats amount string to always show two decimals, e.g. "1" -> "1.00", "12.5" -> "12.50"
    private func normalizeAmountString(_ s: String) -> String {
        var str = s.replacingOccurrences(of: ",", with: ".")
        if let value = Double(str) {
            let nf = NumberFormatter()
            nf.numberStyle = .decimal
            nf.decimalSeparator = "."
            nf.minimumFractionDigits = 2   // force 2 decimals
            nf.maximumFractionDigits = 2
            if let out = nf.string(from: NSNumber(value: value)) {
                str = out
            }
        }
        return str
    }

}
