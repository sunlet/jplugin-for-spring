package net.jplugin.extension.spring;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.ExtensionKernelHelper;
import net.jplugin.core.kernel.api.PluginAnnotation;
import net.jplugin.core.service.api.UseTransaction;
import net.jplugin.extension.spring.autoconfigure.tx.JpluginTransactionInterceptor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Administrator on 2022/5/19.
 */
@PluginAnnotation
public class Jplugin extends AbstractPlugin {

    private static final int PRIORITY = -300;

    public Jplugin() {
        ExtensionKernelHelper.addExtensionInterceptorExtension(this, JpluginTransactionInterceptor.class, null,
                "EP_SERVICE", null, null, UseTransaction.class);
        ExtensionKernelHelper.addExtensionInterceptorExtension(this, JpluginTransactionInterceptor.class, null,
                "EP_SERVICE", null, null, Transactional.class);
    }

    @Override
    public int getPrivority() {
        return PRIORITY;
    }

    @Override
    public void init() {

    }
}
