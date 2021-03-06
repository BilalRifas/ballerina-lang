/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the panic statement context.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class PanicStatementContextProvider extends LSCompletionProvider {
    public PanicStatementContextProvider() {
        this.attachmentPoints.add(BallerinaParser.PanicStatementContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<SymbolInfo> symbolInfoList = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> filteredList = symbolInfoList.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.type instanceof BErrorType)
                .collect(Collectors.toList());

        return this.getCompletionItemList(filteredList, context);
    }
}
