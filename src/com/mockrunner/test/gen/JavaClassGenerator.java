package com.mockrunner.test.gen;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.mockrunner.util.ArrayUtil;
import com.mockrunner.util.ClassUtil;
import com.mockrunner.util.StringUtil;

public class JavaClassGenerator
{
    private Package packageInfo;
    private List imports;
    private String className;
    private Class superClass;
    private List interfaces;
    private List memberTypes;
    private List memberNames;
    private String[] classCommentLines;
    private boolean createJavaDocComments;
    private List methods;
    private List constructors;
    
    public JavaClassGenerator()
    {
        reset();
    }
    
    public void reset()
    {
        imports = new ArrayList();
        interfaces = new ArrayList();
        memberTypes = new ArrayList();
        memberNames = new ArrayList();
        createJavaDocComments = true;
        methods = new ArrayList();
        constructors = new ArrayList();
    }

    public void setCreateJavaDocComments(boolean createJavaDocComments)
    {
        this.createJavaDocComments = createJavaDocComments;
    }
    
    public void setPackage(Package packageInfo)
    {
        this.packageInfo = packageInfo;
    }
    
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    public void setSuperClass(Class superClass)
    {
        this.superClass = superClass;
    }
    
    public void addImport(Class importClass)
    {
        imports.add(importClass.getName());
    }
    
    public void addInterfaceImplementation(Class interfaceClass)
    {
        interfaces.add(interfaceClass);
    }
    
    public void setClassComment(String[] commentLines)
    {
        classCommentLines = (String[])ArrayUtil.copyArray(commentLines);
    }
    
    public void addMemberDeclaration(Class memberType, String name)
    {
        memberTypes.add(memberType);
        memberNames.add(name);
    }
    
    public void addConstructorDeclaration()
    {
        constructors.add(new ConstructorDeclaration());
    }
    
    public void addConstructorDeclaration(ConstructorDeclaration constructor)
    {
        constructors.add(constructor);
    }
    
    public void addMethodDeclaration(MethodDeclaration method)
    {
        methods.add(method);
    }

    public String generate()
    {
        JavaLineAssembler assembler = new JavaLineAssembler();
        assembler.appendPackageInfo(getPackageName());
        appendImportBlocks(assembler);
        appendCommentBlock(assembler, classCommentLines);
        assembler.appendClassDefintion(className, getClassName(superClass), getClassNames(interfaces));
        assembler.appendLeftBrace();
        assembler.appendNewLine();
        assembler.setIndentLevel(1);
        appendMembers(assembler);
        appendConstructors(assembler);
        appendMethods(assembler);
        assembler.setIndentLevel(0);
        assembler.appendRightBrace();
        return assembler.getResult();
    }
    
    private String getPackageName()
    {
        if(null != packageInfo)
        {
            return packageInfo.getName();
        }
        return null;
    }
    
    private String getClassName(Class clazz)
    {
        if(null != superClass)
        {
            return ClassUtil.getClassName(clazz);
        }
        return null;
    }
    
    private String[] getClassNames(List classList)
    {
        if(null == classList || classList.size() <= 0) return null;
        List nameList = new ArrayList();
        for(int ii = 0; ii < classList.size(); ii++)
        {
            Class interfaceClass = (Class)classList.get(ii);
            if(null != interfaceClass)
            {
                nameList.add(ClassUtil.getClassName(interfaceClass));
            }
        }
        return (String[])nameList.toArray(new String[nameList.size()]);
    }
    
    private String[] getClassNames(Class[] arguments)
    {
        if(null == arguments || arguments.length <= 0) return null;
        String[] names = new String[arguments.length];
        for(int ii = 0; ii < arguments.length; ii++)
        {
            Class clazz = arguments[ii];
            names[ii] = ClassUtil.getClassName(clazz);
        }
        return names;
    }
    
    private void appendImportBlocks(JavaLineAssembler assembler)
    {
        List importBlocks = processImports();
        for(int ii = 0; ii < importBlocks.size(); ii++)
        {
            Set currentBlock = (Set)importBlocks.get(ii);
            assembler.appendImports(new ArrayList(currentBlock));
            assembler.appendNewLine();
        }
    }
    
    private void appendCommentBlock(JavaLineAssembler assembler, String[] commentLines)
    {
        if(createJavaDocComments)
        {
            assembler.appendJavaDocComment(commentLines);
        }
        else
        {
            assembler.appendBlockComment(commentLines);
        }
    }

    private void appendMembers(JavaLineAssembler assembler)
    {
        String[] memberTypeNames = getClassNames(memberTypes);
        if(null == memberTypeNames) return;
        for(int ii = 0; ii < memberTypeNames.length; ii++)
        {
            assembler.appendMemberDeclaration(memberTypeNames[ii], (String)memberNames.get(ii));
        }
    }
    
    private void appendMethods(JavaLineAssembler assembler)
    {
        for(int ii = 0; ii < methods.size(); ii++)
        {
            MethodDeclaration declaration = (MethodDeclaration)methods.get(ii);
            appendMethodHeader(assembler, declaration);
            String[] modifiers = prepareModifiers(declaration.getModifier());
            String returnType = getClassName(declaration.getReturnType());
            String[] argumentTypes = getClassNames(declaration.getArguments());
            assembler.appendMethodDeclaration(modifiers, returnType, declaration.getName(), argumentTypes, declaration.getArgumentNames());
            appendMethodBody(assembler, declaration);
        }
    }
    
    private void appendConstructors(JavaLineAssembler assembler)
    {
        for(int ii = 0; ii < constructors.size(); ii++)
        {
            ConstructorDeclaration declaration = (ConstructorDeclaration)constructors.get(ii);
            appendMethodHeader(assembler, declaration);
            String[] argumentTypes = getClassNames(declaration.getArguments());
            assembler.appendConstructorDeclaration(className, argumentTypes, declaration.getArgumentNames());
            appendMethodBody(assembler, declaration);
        }
    }
    
    private void appendMethodHeader(JavaLineAssembler assembler, ConstructorDeclaration declaration)
    {
        assembler.appendNewLine();
        assembler.appendBlockComment(declaration.getCommentLines());
    }

    private void appendMethodBody(JavaLineAssembler assembler, ConstructorDeclaration declaration)
    {
        assembler.appendIndent();
        assembler.appendLeftBrace();
        assembler.appendNewLine();
        appendCodeLines(assembler, declaration.getCodeLines());
        assembler.appendIndent();
        assembler.appendRightBrace();
        assembler.appendNewLine();
    }

    private void appendCodeLines(JavaLineAssembler assembler, String[] codeLines)
    {
        assembler.setIndentLevel(2);
        assembler.appendCodeLines(codeLines);
        assembler.setIndentLevel(1);
    }
    
    private String[] prepareModifiers(int modifier)
    {
        String modifierString = Modifier.toString(modifier);
        if(null == modifierString || modifierString.trim().length() <= 0) return null;
        return StringUtil.split(modifierString, " ", true);
    }
    
    private List processImports()
    {
        addMissingImports();
        Map blocks = prepareImportBlocks();
        return sortImportBlocks(blocks);
    }
    
    private List sortImportBlocks(Map blocks)
    {
        List sortedBlockKeys = new ArrayList();
        addDefaultDomains(blocks, sortedBlockKeys);
        addNonDefaultDomains(blocks, sortedBlockKeys);
        List sortedBlocks = new ArrayList();
        for(int ii = 0; ii < sortedBlockKeys.size(); ii++)
        {
            String key = (String)sortedBlockKeys.get(ii);
            Set currentBlock = (Set)blocks.get(key);
            sortedBlocks.add(currentBlock);
        }
        return sortedBlocks;
    }
    
    private void addNonDefaultDomains(Map blocks, List sortedBlockKeys)
    {
        Iterator keys = blocks.keySet().iterator();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            if(!(key.equals("java") || key.equals("javax") || key.equals("org") || key.equals("com")))
            {
                addLexicographically(sortedBlockKeys, key);
            }
        }
    }
    
    private void addDefaultDomains(Map blocks, List sortedBlockKeys)
    {
        Iterator keys = blocks.keySet().iterator();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            if(key.equals("java"))
            {
                addAtIndex(sortedBlockKeys, 0, key);
            }
            if(key.equals("javax"))
            {
                addAtIndex(sortedBlockKeys, 1, key);
            }
            if(key.equals("org"))
            {
                addAtIndex(sortedBlockKeys, 2, key);
            }
            if(key.equals("com"))
            {
                addAtIndex(sortedBlockKeys, 3, key);
            }
        }
    }
    
    private void addLexicographically(List sortedBlockKeys, String key)
    {
        for(int ii = 0; ii < sortedBlockKeys.size(); ii++)
        {
            String currentBlockKey = (String)sortedBlockKeys.get(ii);
            if(currentBlockKey.compareTo(key) > 0)
            {
                addAtIndex(sortedBlockKeys, ii - 1, key);
                return;
            }
        }
        sortedBlockKeys.add(key);
    }

    private void addAtIndex(List list, int index, Object object)
    {
        if(index < 0)
        {
            index = 0;
        }
        if(index < list.size())
        {
            list.add(index, object);
        }
        else
        {
            list.add(object);
        }
    }
    
    private Map prepareImportBlocks()
    {
        Map importBlockMap = new HashMap();
        for(int ii = 0; ii < imports.size(); ii++)
        {
            String blockKey = getBlockKey((String)imports.get(ii));
            Set blockSet = (Set)importBlockMap.get(blockKey);
            if(null == blockSet)
            {
                blockSet = new TreeSet();
                importBlockMap.put(blockKey, blockSet);
            }
            blockSet.add(imports.get(ii));
        }
        return importBlockMap;
    }
    
    private String getBlockKey(String packageInfo)
    {
        if(null == packageInfo || packageInfo.trim().length() <= 0) return "";
        int index = packageInfo.indexOf('.');
        if(index < 0) return packageInfo;
        return packageInfo.substring(0, index);
    }
    
    private void addMissingImports()
    {
        addImportIfNecessary(superClass);
        addImportsIfNecessary(interfaces);
        addImportsIfNecessary(memberTypes);
        for(int ii = 0; ii < constructors.size(); ii++)
        {
            ConstructorDeclaration declaration = (ConstructorDeclaration)constructors.get(ii);
            addImportsForArguments(declaration);
        }
        for(int ii = 0; ii < methods.size(); ii++)
        {
            MethodDeclaration declaration = (MethodDeclaration)methods.get(ii);
            addImportForReturnType(declaration);
            addImportsForArguments(declaration);
        }
    }

    private void addImportsForArguments(ConstructorDeclaration declaration)
    {
        Class[] arguments = declaration.getArguments();
        if(null == arguments || arguments.length <= 0) return;
        for(int ii = 0; ii < arguments.length; ii++)
        {
            addImportIfNecessary(arguments[ii]);
        }
    }
    
    private void addImportForReturnType(MethodDeclaration declaration)
    {
        Class returnType = declaration.getReturnType();
        if(null == returnType) return;
        addImportIfNecessary(returnType);
    }
    
    private void addImportsIfNecessary(List classes)
    {
        if(null == classes) return;
        for(int ii = 0; ii < classes.size(); ii++)
        {
            addImportIfNecessary((Class)classes.get(ii));
        }
    }

    private void addImportIfNecessary(Class clazz)
    {
        if(null == clazz) return;
        if(imports.contains(clazz.getName())) return;
        if(clazz.getName().startsWith("java.lang")) return;
        if(belongsToSamePackage(clazz)) return;
        if(clazz.isPrimitive()) return;
        addImport(clazz);
    }
    
    private boolean belongsToSamePackage(Class clazz)
    {
        String thisPackageName = getPackageName();
        Package classPackage = clazz.getPackage();
        String classPackageName = "";
        if(null != classPackage)
        {
            classPackageName = classPackage.getName();
        }
        if(null == thisPackageName)
        {
            thisPackageName = "";
        }
        if(null == classPackageName)
        {
            classPackageName = "";
        }
        return thisPackageName.equals(classPackageName);
    }
    
    public static class ConstructorDeclaration
    {
        private Class[] arguments;
        private String[] argumentNames;
        private String[] codeLines;
        private String[] commentLines;

        public String[] getCodeLines()
        {
            if(null == codeLines) return null;
            return (String[])ArrayUtil.copyArray(codeLines);
        }
        
        public void setCodeLines(String[] codeLines)
        {
            this.codeLines = (String[])ArrayUtil.copyArray(codeLines);
        }
        
        public String[] getCommentLines()
        {
            if(null == commentLines) return null;
            return (String[])ArrayUtil.copyArray(commentLines);
        }
        
        public void setCommentLines(String[] commentLines)
        {
            this.commentLines = (String[])ArrayUtil.copyArray(commentLines);
        }
       
        public String[] getArgumentNames()
        {
            if(null == argumentNames) return null;
            return (String[])ArrayUtil.copyArray(argumentNames);
        }
        
        public void setArgumentNames(String[] argumentNames)
        {
            this.argumentNames = (String[])ArrayUtil.copyArray(argumentNames);
        }
        
        public Class[] getArguments()
        {
            if(null == arguments) return null;
            return (Class[])ArrayUtil.copyArray(arguments);
        }
        
        public void setArguments(Class[] arguments)
        {
            this.arguments = (Class[])ArrayUtil.copyArray(arguments);
        }
    }
    
    public static class MethodDeclaration extends ConstructorDeclaration
    {
        private int modifier;
        private Class returnType;
        private String name;
  
        public MethodDeclaration()
        {
            this("method");
        }
        
        public MethodDeclaration(String name)
        {
            this(Modifier.PUBLIC, name);
        }
        
        public MethodDeclaration(int modifier, String name)
        {
            this(modifier, name, Void.TYPE);
        }
        
        public MethodDeclaration(int modifier, String name, Class returnType)
        {
            setModifier(modifier);
            setReturnType(returnType);
            setName(name);
        }

        public int getModifier()
        {
            return modifier;
        }
        
        public void setModifier(int modifier)
        {
            this.modifier = modifier;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public Class getReturnType()
        {
            return returnType;
        }
        
        public void setReturnType(Class returnType)
        {
            this.returnType = returnType;
        }
    }
}
