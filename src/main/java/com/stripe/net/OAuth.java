package com.stripe.net;

import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.oauth.DeauthorizedAccount;
import com.stripe.model.oauth.TokenResponse;
import java.util.HashMap;
import java.util.Map;

public final class OAuth {
  private static StripeResponseGetter globalResponseGetter = new LiveStripeResponseGetter();

  public static void setGlobalResponseGetter(StripeResponseGetter srg) {
    OAuth.globalResponseGetter = srg;
  }

  /**
   * Generates a URL to Stripe's OAuth form.
   *
   * @param params the parameters to include in the URL.
   * @param options the request options.
   * @return the URL to Stripe's OAuth form.
   */
  public static String authorizeUrl(Map<String, Object> params, RequestOptions options)
      throws AuthenticationException, InvalidRequestException {
    final String base = Stripe.getConnectBase();

    params.put("client_id", getClientId(params, options));
    if (params.get("response_type") == null) {
      params.put("response_type", "code");
    }
    String query = FormEncoder.createQueryString(params);
    String url = base + "/oauth/authorize?" + query;
    return url;
  }

  /**
   * Uses an authorization code to connect an account to your platform and fetch the user's
   * credentials.
   *
   * @param params the request parameters.
   * @param options the request options.
   * @return the TokenResponse instance containing the response from the OAuth API.
   */
  public static TokenResponse token(Map<String, Object> params, RequestOptions options)
      throws StripeException {
    ApiRequest request =
        new ApiRequest(
            BaseAddress.CONNECT, ApiResource.RequestMethod.POST, "/oauth/token", params, options);
    return OAuth.globalResponseGetter.request(request, TokenResponse.class);
  }

  /**
   * Disconnects an account from your platform.
   *
   * @param params the request parameters.
   * @param options the request options.
   * @return the DeauthorizedAccount instance containing the response from the OAuth API.
   */
  public static DeauthorizedAccount deauthorize(Map<String, Object> params, RequestOptions options)
      throws StripeException {
    Map<String, Object> paramsCopy = new HashMap<>();
    paramsCopy.putAll(params);

    paramsCopy.put("client_id", getClientId(paramsCopy, options));
    ApiRequest request =
        new ApiRequest(
            BaseAddress.CONNECT,
            ApiResource.RequestMethod.POST,
            "/oauth/deauthorize",
            paramsCopy,
            options);
    return OAuth.globalResponseGetter.request(request, DeauthorizedAccount.class);
  }

  /**
   * Returns the client_id to use in OAuth requests.
   *
   * @param params the request parameters.
   * @param options the request options.
   * @return the client_id.
   */
  private static String getClientId(Map<String, Object> params, RequestOptions options)
      throws AuthenticationException {
    String clientId = Stripe.clientId;
    if ((options != null) && (options.getClientId() != null)) {
      clientId = options.getClientId();
    }
    if ((params != null) && (params.get("client_id") != null)) {
      clientId = (String) params.get("client_id");
    }

    if (clientId == null) {
      throw new AuthenticationException(
          "No client_id provided. (HINT: set client_id key using 'Stripe.clientId = <CLIENT-ID>'. "
              + "You can find your client_ids in your Stripe dashboard at "
              + "https://dashboard.stripe.com/account/applications/settings, "
              + "after registering your account as a platform. See "
              + "https://stripe.com/docs/connect/standard-accounts for details, "
              + "or email support@stripe.com if you have any questions.",
          null,
          null,
          0);
    }

    return clientId;
  }
}
