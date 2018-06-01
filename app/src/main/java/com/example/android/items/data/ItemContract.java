/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.items.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ItemContract {
    private ItemContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.items";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public final static String TABLE_NAME = "items";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME ="name";
        public final static String COLUMN_ITEM_TYPE = "type";
        public final static String COLUMN_ITEM_SIZE = "size";
        public final static String COLUMN_ITEM_VALUE = "value";
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_ITEM_PICTURE = "picture";
        public static final int ITEM_UNKNOWN = 0;
        public static final int ITEM_LARGE = 1;
        public static final int ITEM_SMALL = 2;
        public static boolean isValidSize(int item) {
            if (item == ITEM_UNKNOWN || item == ITEM_LARGE || item == ITEM_SMALL) {
                return true;
            }
            return false;
        }
    }
}

