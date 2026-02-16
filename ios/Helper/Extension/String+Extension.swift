//
//  String+Extension.swift
//  GluedInMedia
//
//  Created by Ashish on 13/02/26.
//

import Foundation

extension String {
  
    func localized(comment: String = "") -> String {
        return NSLocalizedString(self, comment: comment)
    }

    func localizedWithDefaultValue(defaultValue: String, comment: String = "") -> String {
        return NSLocalizedString(
            self,
            tableName: nil,
            bundle: Bundle.main,
            value: defaultValue,
            comment: comment
        )
    }
    
}
