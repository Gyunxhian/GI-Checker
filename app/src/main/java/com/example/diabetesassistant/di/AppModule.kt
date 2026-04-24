package com.example.diabetesassistant.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt DI 主模块占位。
 * 阶段二数据层完成后，在此绑定 Repository、数据库、网络服务等依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
