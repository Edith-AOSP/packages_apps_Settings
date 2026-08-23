/*
 * Copyright (C) 2021 Project Radiant
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

package org.edith.settings.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class WallpaperBlurView extends ImageView {

    public WallpaperBlurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public WallpaperBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WallpaperBlurView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.CENTER_CROP);
        setRenderEffect(RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP));
        WallpaperManager wm = WallpaperManager.getInstance(context);
        setImageDrawable(wm.getDrawable());
    }
}
