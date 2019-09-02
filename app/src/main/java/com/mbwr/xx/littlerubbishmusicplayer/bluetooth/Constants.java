/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbwr.xx.littlerubbishmusicplayer.bluetooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // 请求类型,长度需一致 3 字符
    String MESSAGE_REQUEST_FILE_LIST = "006";
    String MESSAGE_RESPONSE_FILE_LIST = "007";

    String MESSAGE_REQUEST_DOWNLOAD_FILE = "008";//请求下载文件信息
    String MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE = "009";//回应文件信息

    String MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK = "010";//请求下载文件块
    String MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK = "011";//回应文件块请求


    String MESSAGE_RESPONSE_OK = "200";
    String MESSAGE_SOURCE_NOT_EXIST = "404";

    //分割文件信息 数据大小
    int BLOCK_SIZE = 65536;//64k one block
    int INDEX_LENGTH = 4;//文件块索引所占字节 3: max 4096块,

    //单次写入蓝牙数据流最大字节大小
    int MAX_SIZE_WRITE_INPUT = 1024;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

}
