package com.tallyto.gestorfinanceiro.testsupport;

import com.tallyto.gestorfinanceiro.config.JwtAuthenticationFilter;
import com.tallyto.gestorfinanceiro.config.TenantFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Anotação composta para testes de controller MVC que:
 * - Isola o slice do WebMvc (@WebMvcTest)
 * - Exclui filtros globais (TenantFilter, JwtAuthenticationFilter)
 * - Desabilita filtros do Spring Security no MockMvc
 * - Importa mocks de segurança padrão para o contexto de teste
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest(
        controllers = {},
        excludeFilters = {
                @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TenantFilter.class),
                @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityMocks.class)
public @interface ControllerSliceTest {

    /**
     * Permite declarar o(s) controller(s) sob teste e repassa para o @WebMvcTest interno.
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
