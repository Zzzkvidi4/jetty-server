package com.zzzkvidi4.server;

import com.google.gson.Gson;
import com.zzzkvidi4.dal.tables.daos.ProductDao;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.postgresql.ds.PGSimpleDataSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ProductServlet extends HttpServlet {
    @NotNull
    private final ProductDao productDao;

    {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/server");
        productDao = new ProductDao(
                new DefaultConfiguration()
                        .derive(SQLDialect.POSTGRES_9_4)
                        .derive(new DataSourceConnectionProvider(dataSource))
        );
    }

    @Override
    protected void doGet(@NotNull HttpServletRequest req, @NotNull HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        gson.toJson(productDao.findAll(), resp.getWriter());
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest req, @NotNull HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        ProductDto productDto = gson.fromJson(req.getReader(), ProductDto.class);
        productDao.insert(productDto.toEntity());
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.encodeRedirectURL("file/index.html");
    }
}
