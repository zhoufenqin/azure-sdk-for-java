// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.serialization.hybridrow.layouts;

import com.azure.cosmos.core.Out;
import com.azure.cosmos.serialization.hybridrow.Result;
import com.azure.cosmos.serialization.hybridrow.RowBuffer;
import com.azure.cosmos.serialization.hybridrow.RowCursor;

import javax.annotation.Nonnull;

public final class LayoutArray extends LayoutIndexedScope {

    public LayoutArray(final boolean immutable) {
        super(immutable
            ? LayoutCode.IMMUTABLE_ARRAY_SCOPE
            : LayoutCode.ARRAY_SCOPE, immutable, false, false, false, false);
    }

    @Nonnull
    public String name() {
        return this.isImmutable() ? "im_array" : "array";
    }

    @Override
    @Nonnull
    public Result writeScope(
        @Nonnull final RowBuffer buffer,
        @Nonnull final RowCursor edit,
        @Nonnull final TypeArgumentList typeArgs,
        @Nonnull Out<RowCursor> value) {
        return this.writeScope(buffer, edit, typeArgs, UpdateOptions.UPSERT, value);
    }

    @Override
    @Nonnull
    public Result writeScope(
        @Nonnull RowBuffer buffer, @Nonnull RowCursor edit, @Nonnull TypeArgumentList typeArgs,
        @Nonnull UpdateOptions options, @Nonnull Out<RowCursor> value) {

        Result result = prepareSparseWrite(buffer, edit, this.typeArg(), options);

        if (result != Result.SUCCESS) {
            value.set(null);
            return result;
        }

        buffer.writeSparseArray(edit, this, options);
        return Result.SUCCESS;
    }
}
