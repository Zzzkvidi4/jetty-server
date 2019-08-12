package com.zzzkvidi4.server;

import com.zzzkvidi4.dal.tables.pojos.Product;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter(onParam_ = @NotNull)
public final class ProductDto {
    @Nullable
    private String name;
    @Nullable
    private String organization;
    private int volume;

    @NotNull
    public Product toEntity() {
        return new Product(null, name, organization, volume);
    }
}
