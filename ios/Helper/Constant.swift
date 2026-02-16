//
//  Constant.swift
//  GluedInMedia
//
//  Created by Ashish on 13/02/26.
//

import Foundation

enum PreferenceKey: String {
    case emailId = "emailId"
    case password = "password"

    var localizedString:String {
        return NSLocalizedString(rawValue, comment: "")
    }
}

enum EventTypes : String{
  case hashTagClick = "hashtag"
  case challengeClick = "challenge"
}

enum ShopifyConfig {
    static let shopDomain   = "gluedin-development.myshopify.com"      // ← change
    static let accessToken  = "9c59ca7a76847ae67136df4c9c558aad"      // ← change
}
