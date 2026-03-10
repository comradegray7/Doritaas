package com.example.myapp.data.modules

import android.content.Context
import com.example.myapp.data.api.PaymentApi
import com.stripe.android.PaymentConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Hilt module for Stripe integration dependencies.
 *
 * Configures Retrofit for communicating with payment backend and provides [PaymentApi].
 * Also handles initialization of the Stripe SDK's [PaymentConfiguration].
 *
 * Note: Requires a backend server URL (currently pointing to ngrok for development).
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * StripeModule
 *
 */
object StripeModule {

    // IMPORTANT: Replace this with your actual ngrok or server URL
    private const val BASE_URL =  "https://ethelyn-nonsoluble-zaria.ngrok-free.dev"

    @Provides
    /**
     * providePaymentConfiguration
     *
     *
     * @param @ApplicationContext context The @ApplicationContext context parameter
     */
    fun providePaymentConfiguration(@ApplicationContext context: Context): PaymentConfiguration {
        // NOTE: The actual PaymentConfiguration.init() will be called in the Composable
        // with the publishableKey fetched from the server.
        // We initialize it here with a placeholder for DI.

        return PaymentConfiguration.getInstance(context)
    }

    @Provides
    /**
     * provideRetrofit
     *
     */
    fun provideRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")  // Important!
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    /**
     * providePaymentApi
     *
     *
     * @param retrofit The retrofit parameter
     */
    fun providePaymentApi(retrofit: Retrofit): PaymentApi {
        return retrofit.create(PaymentApi::class.java)
    }
}
