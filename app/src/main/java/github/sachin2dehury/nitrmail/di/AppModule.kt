package github.sachin2dehury.nitrmail.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import github.sachin2dehury.nitrmail.adapters.MailBoxAdapter
import github.sachin2dehury.nitrmail.api.calls.BasicAuthInterceptor
import github.sachin2dehury.nitrmail.api.calls.MailApi
import github.sachin2dehury.nitrmail.api.calls.MailViewClient
import github.sachin2dehury.nitrmail.api.database.MailDao
import github.sachin2dehury.nitrmail.api.database.MailDatabase
import github.sachin2dehury.nitrmail.others.Constants
import github.sachin2dehury.nitrmail.others.DataStoreExt
import github.sachin2dehury.nitrmail.others.InternetChecker
import github.sachin2dehury.nitrmail.others.NetworkBoundResource
import github.sachin2dehury.nitrmail.repository.Repository
import github.sachin2dehury.nitrmail.services.SyncBroadcastReceiver
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()

    @Singleton
    @Provides
    fun provideOkHttpClient(basicAuthInterceptor: BasicAuthInterceptor) = OkHttpClient.Builder()
        .addInterceptor(basicAuthInterceptor)
        .build()

    @Singleton
    @Provides
    fun provideMailViewClient() = MailViewClient()

    @Singleton
    @Provides
    fun provideMailApi(
        okHttpClient: OkHttpClient
    ): MailApi = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(MailApi::class.java)

    @Provides
    @Singleton
    fun provideMailBoxAdapter(@ApplicationContext context: Context) = MailBoxAdapter(context)

    @Singleton
    @Provides
    fun provideMailDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, MailDatabase::class.java, Constants.DATABASE_NAME)
        .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideMailDao(mailDatabase: MailDatabase) = mailDatabase.getMailDao()

    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context
    ) = DataStoreExt(context)

    @Singleton
    @Provides
    fun provideNetworkBoundResource() = NetworkBoundResource()

    @Singleton
    @Provides
    fun provideInternetChecker(@ApplicationContext context: Context) = InternetChecker(context)

    @Singleton
    @Provides
    fun provideSyncBroadcastReceiver() = SyncBroadcastReceiver()

    @Singleton
    @Provides
    fun provideRepository(
        basicAuthInterceptor: BasicAuthInterceptor,
        dataStore: DataStoreExt,
        internetChecker: InternetChecker,
        mailApi: MailApi,
        mailDao: MailDao,
        networkBoundResource: NetworkBoundResource,
    ) = Repository(
        basicAuthInterceptor,
        dataStore,
        internetChecker,
        mailApi,
        mailDao,
        networkBoundResource
    )
}