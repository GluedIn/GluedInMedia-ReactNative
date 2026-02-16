//
//  Global.swift
//  MediaSampleApp
//
//  Created by Abhishek Mishra on 10/09/25.
//

import Foundation
import MobileBuySDK


/// A singleton class providing global utility methods.
class Global {
  
  /// The shared singleton instance.
  static let shared = Global()
  
  // MARK: - Shopify (Storefront API via MobileBuySDK)
  /// GraphQL Storefront client configured with shop domain + access token.
  let shopifyClient = Graph.Client(
    shopDomain: ShopifyConfig.shopDomain,
    apiKey: ShopifyConfig.accessToken
  )
  
  /// UserDefaults keys for persisting cart identifiers/URLs.
  private let kShopifyCartIdKey = "gi.shopify.cartId"
  private let kShopifyCartUrlKey = "gi.shopify.cartUrl"
  
  // MARK: - Shopify Cart (Storefront API-backed)
  //
  // Persist cart details in UserDefaults so they survive app restarts.
  // Accessing these properties always reads the saved values.
  
  /// The current Shopify cart GID (e.g., "gid://shopify/Cart/...").
  private var shopifyCartId: String? {
    get { UserDefaults.standard.string(forKey: kShopifyCartIdKey) }
    set {
      if let v = newValue, !v.isEmpty {
        UserDefaults.standard.set(v, forKey: kShopifyCartIdKey)
      } else {
        UserDefaults.standard.removeObject(forKey: kShopifyCartIdKey)
      }
    }
  }
  
  /// Last-known cart URL; may be a cart permalink or checkout URL depending on flow.
  private var shopifyCartUrl: String? {
    get { UserDefaults.standard.string(forKey: kShopifyCartUrlKey) }
    set {
      if let v = newValue, !v.isEmpty {
        UserDefaults.standard.set(v, forKey: kShopifyCartUrlKey)
      } else {
        UserDefaults.standard.removeObject(forKey: kShopifyCartUrlKey)
      }
    }
  }
  
  
  func getUserEmailId() -> String? {
    let emailId = UserDefaults.standard.object(forKey: PreferenceKey.emailId.localizedString) as? String
    return emailId
  }
  
  func getUserPassword() -> String? {
    let password = UserDefaults.standard.object(forKey: PreferenceKey.password.localizedString) as? String
    return password
  }
  
  func setUserDetails(emailId: String?, password: String?) {
    UserDefaults.standard.set(emailId, forKey: PreferenceKey.emailId.localizedString)
    UserDefaults.standard.set(password, forKey: PreferenceKey.password.localizedString)
  }
  
  func getGIDClientID() -> String {
    return "615285002663-dth1ucmv8ulo9nbn6gs34ncah3b9lk9r.apps.googleusercontent.com"
  }
  
  /**
   Retrieves a localized string for the specified key.
   
   This method attempts to fetch the localized value for a given key using a string extension method. If no localized value is found,
   it returns the provided default string.
   
   - Parameters:
   - localisedKey: The key used to look up the localized value.
   - defaultString: The fallback string to return if the localization for the key is missing.
   
   - Returns: A localized string if available; otherwise, the default string.
   */
  func getLocalisedValue(localisedKey: String, defaultString: String) -> String {
    return localisedKey.localizedWithDefaultValue(defaultValue: defaultString)
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
  
  
  // MARK: - Shopify Product (Single)
  /// Fetch a single Shopify product by its GID and map to `ProductVM`.
  /// - Parameter productId: Example "gid://shopify/Product/1234567890"
  func fetchProductDetails(
    productId: String,
    completion: @escaping (Result<ProductVM, Error>) -> Void
  ) {
    let query = makeProductByIdQuery(productId)
    
    let task  = shopifyClient.queryGraphWith(query) { [weak self] response, error in
      guard let self = self else { return }
      
      if let error {
        completion(.failure(error))
        return
      }
      
      // `node` is a union/interface; cast to Product (works across SDK versions)
      guard let productNode = response?.node as? MobileBuySDK.Storefront.Product else {
        let notFound = NSError(
          domain: "Shopify",
          code: 404,
          userInfo: [NSLocalizedDescriptionKey: "Product not found for id \(productId)"]
        )
        completion(.failure(notFound))
        return
      }
      
      let vm = self.mapProduct(productNode)
      completion(.success(vm))
    }
    task.resume()
  }
  
  /// GraphQL query builder for fetching a product by GID.
  func makeProductByIdQuery(_ productId: String) -> MobileBuySDK.Storefront.QueryRootQuery {
    let gid = GraphQL.ID(rawValue: productId)
    return Storefront.buildQuery { $0
      .node(id: gid) { $0
        .onProduct { $0
          .id()
          .title()
          .handle()
          .description()
          .images(first: 1) { $0
            .edges { $0
              .node { $0
                .url()
                .altText()
              }
            }
          }
          .priceRange { $0
            .minVariantPrice { $0
              .amount()
              .currencyCode()
            }
            .maxVariantPrice { $0
              .amount()
              .currencyCode()
            }
          }
          .options(first: 20) { $0
            .name()
            .values()
          }
          .variants(first: 20) { $0
            .edges { $0
              .node { $0
                .id()
                .title()
                .price { $0
                  .amount()
                  .currencyCode()
                }
                .availableForSale()
                .image { $0
                  .url()
                  .altText()
                }
              }
            }
          }
        }
      }
    }
  }
  
  /// Maps Storefront Product to lightweight `ProductVM` used by the app.
  private func mapProduct(_ node: MobileBuySDK.Storefront.Product) -> ProductVM {
    let firstImageURL: URL? = node.images.edges.first?.node.url
    let min = moneyString(node.priceRange.minVariantPrice)
    let max = moneyString(node.priceRange.maxVariantPrice)
    
    // Grab the first option name, or detect size-like option
    let optionNames = node.options.map { $0.name }
    let variantName: String? = optionNames.first  // or use a helper to prefer "Size"
    
    let variants = node.variants.edges.map { mapVariant($0.node, productImageURL: firstImageURL) }
    return ProductVM(
      id: node.id.rawValue,
      title: node.title,
      handle: node.handle,
      description: node.description,
      imageURL: firstImageURL,
      minPrice: min,
      maxPrice: max,
      variants: variants,
      variantName: variantName
    )
  }
  
  /// Maps Storefront ProductVariant to `VariantVM`.
  private func mapVariant(
    _ node: MobileBuySDK.Storefront.ProductVariant,
    productImageURL: URL?
  ) -> VariantVM {
    let variantImageURL: URL? = node.image?.url ?? productImageURL
    return VariantVM(
      id: node.id.rawValue,
      title: node.title,
      price: moneyString(node.price),
      available: node.availableForSale,
      imageURL: variantImageURL
    )
  }
  
  /// Formats MoneyV2 into "CUR 0.00" string.
  private func moneyString(_ money: MobileBuySDK.Storefront.MoneyV2) -> String {
    let amount = NSDecimalNumber(decimal: money.amount).doubleValue
    return String(format: "%@ %.2f", money.currencyCode.rawValue, amount)
  }
  
  func fetchCartItemsQuantity(completion: @escaping (Result<Int, Error>) -> Void) {
    guard let id = shopifyCartId else {
      completion(.success(0))
      return
    }
    let q = Storefront.buildQuery { $0
      .cart(id: GraphQL.ID(rawValue: id)) { $0
        .id()
        .totalQuantity()
      }
    }
    let task = shopifyClient.queryGraphWith(q) { response, error in
      if let error = error {
        completion(.failure(error))
        return
      }
      let qty = response?.cart?.totalQuantity ?? 0
      completion(.success(Int(qty)))
    }
    task.resume()
  }
  
  // MARK: - Shopify Cart Mutations
  /// Adds a ProductVariant to the current cart (creates cart if missing).
  /// - Parameters:
  ///   - variantId: `gid://shopify/ProductVariant/...`
  ///   - quantity: Quantity to add (default 1)
  ///   - completion: Returns checkoutUrl and totalQuantity after mutation
  func addVariantToShopifyCart(
    variantId: String,
    quantity: Int32 = 1,
    completion: @escaping (Result<(checkoutUrl: String, totalQuantity: Int32), Error>) -> Void
  ) {
    createCartIfNeeded { [weak self] result in
      guard let self = self else { return }
      switch result {
      case .failure(let err):
        completion(.failure(err))
      case .success(let info):
        let cartGID = GraphQL.ID(rawValue: info.cartId)
        
        // Build line input
        let line = Storefront.CartLineInput.create(
          merchandiseId: GraphQL.ID(rawValue: variantId),
          quantity: Input.value(quantity)
        )
        
        let mutation = Storefront.buildMutation { $0
          .cartLinesAdd(cartId: cartGID, lines: [line]) { $0
            .cart { $0
              .id()
              .checkoutUrl()
              .totalQuantity()
            }
            .userErrors { $0
              .field()
              .message()
            }
          }
        }
        
        let task = self.shopifyClient.mutateGraphWith(mutation) { response, error in
          if let error = error {
            completion(.failure(error))
            return
          }
          guard
            let added = response?.cartLinesAdd,
            let cart = added.cart
          else {
            let msg = response?.cartLinesAdd?.userErrors.first?.message ?? "Failed to add line to cart."
            completion(.failure(NSError(domain: "Shopify", code: 0, userInfo: [NSLocalizedDescriptionKey: msg])))
            return
          }
          // Update local cart id if necessary
          self.shopifyCartId = cart.id.rawValue
          completion(.success((cart.checkoutUrl.absoluteString, cart.totalQuantity)))
        }
        task.resume()
      }
    }
  }
  
  /// Ensures a cart exists, creating one if needed, and returns identifiers.
  /// - Returns: `cartId`, `checkoutUrl`, and `totalQuantity`
  func createCartIfNeeded(
    completion: @escaping (
      Result<(
        cartId: String,
        checkoutUrl: String,
        totalQuantity: Int32),
      Error>) -> Void
  ) {
    if let cartId = shopifyCartId {
      // We don't fetch; we just surface what we have. Caller can proceed to add lines.
      completion(.success((cartId, "", 0))) // checkoutUrl/quantity will be refreshed on add
      return
    }
    
    let mutation = Storefront.buildMutation { $0
      .cartCreate(input: Storefront.CartInput.create()) { $0
        .cart { $0
          .id()
          .checkoutUrl()
          .totalQuantity()
        }
        .userErrors { $0
          .field()
          .message()
        }
      }
    }
    
    let task = shopifyClient.mutateGraphWith(mutation) { [weak self] response, error in
      guard let self = self else { return }
      if let error = error {
        completion(.failure(error))
        return
      }
      guard
        let cartCreate = response?.cartCreate,
        let cart = cartCreate.cart
      else {
        let msg = response?.cartCreate?.userErrors.first?.message ?? "Failed to create cart."
        completion(.failure(NSError(domain: "Shopify", code: 0, userInfo: [NSLocalizedDescriptionKey: msg])))
        return
      }
      let id = cart.id.rawValue
      self.shopifyCartId = id
      self.shopifyCartUrl = cart.checkoutUrl.absoluteString
      completion(.success((id, cart.checkoutUrl.absoluteString, cart.totalQuantity)))
    }
    task.resume()
  }
  
  func setShopifyCartUrl(shopifyCartUrl: String) {
    self.shopifyCartUrl = shopifyCartUrl
  }
  
  func setShopifyCartId(shopifyCartId: String) {
    self.shopifyCartId = shopifyCartId
  }
  
  func getShopifyCartUrl() -> String? {
    return self.shopifyCartUrl
  }
  
  func getShopifyCartId() -> String? {
    return self.shopifyCartId
  }
  
}

struct VariantVM {
    let id: String
    let title: String
    let price: String
    let available: Bool
    let imageURL: URL?
}

struct ProductVM {
    let id: String
    let title: String
    let handle: String
    let description: String
    let imageURL: URL?
    let minPrice: String
    let maxPrice: String
    let variants: [VariantVM]
    let variantName: String?
}
