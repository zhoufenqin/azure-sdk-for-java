// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

public final class BatchSchemaProvider {
    private static LayoutResolverNamespace BatchLayoutResolver;
    private static Layout BatchOperationLayout;
    private static Layout BatchResultLayout;
    private static Namespace BatchSchemaNamespace;

    static {
        String json = BatchSchemaProvider.GetEmbeddedResource("Batch\\HybridRowBatchSchemas.json");
        BatchSchemaProvider.setBatchSchemaNamespace(Namespace.Parse(json));
        BatchSchemaProvider.setBatchLayoutResolver(new LayoutResolverNamespace(BatchSchemaProvider.getBatchSchemaNamespace()));

        BatchSchemaProvider.setBatchOperationLayout(BatchSchemaProvider.getBatchLayoutResolver().Resolve(BatchSchemaProvider.getBatchSchemaNamespace().Schemas.Find(x -> x.Name.equals("BatchOperation")).SchemaId));
        BatchSchemaProvider.setBatchResultLayout(BatchSchemaProvider.getBatchLayoutResolver().Resolve(BatchSchemaProvider.getBatchSchemaNamespace().Schemas.Find(x -> x.Name.equals("BatchResult")).SchemaId));
    }

    public static LayoutResolverNamespace getBatchLayoutResolver() {
        return BatchLayoutResolver;
    }

    private static void setBatchLayoutResolver(LayoutResolverNamespace value) {
        BatchLayoutResolver = value;
    }

    public static Layout getBatchOperationLayout() {
        return BatchOperationLayout;
    }

    private static void setBatchOperationLayout(Layout value) {
        BatchOperationLayout = value;
    }

    public static Layout getBatchResultLayout() {
        return BatchResultLayout;
    }

    private static void setBatchResultLayout(Layout value) {
        BatchResultLayout = value;
    }

    public static Namespace getBatchSchemaNamespace() {
        return BatchSchemaNamespace;
    }

    private static void setBatchSchemaNamespace(Namespace value) {
        BatchSchemaNamespace = value;
    }

    private static String FormatResourceName(String namespaceName, String resourceName) {
        return namespaceName + "." + resourceName.replace(" ", "_").replace("\\", ".").replace("/", ".");
    }

    private static String GetEmbeddedResource(String resourceName) {
        Assembly assembly = Assembly.GetAssembly(BatchSchemaProvider.class);

        // Assumes BatchSchemaProvider is in the default namespace of the assembly.
        resourceName = BatchSchemaProvider.FormatResourceName(BatchSchemaProvider.class.Namespace, resourceName);

        try (Stream resourceStream = assembly.GetManifestResourceStream(resourceName)) {
            if (resourceStream == null) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(resourceStream)) {
                return reader.ReadToEnd();
            }
        }
    }
}
