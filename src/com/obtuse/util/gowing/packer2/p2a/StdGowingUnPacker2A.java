package com.obtuse.util.gowing.packer2.p2a;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.BasicProgramConfigInfo;
import com.obtuse.util.Logger;
import com.obtuse.util.ObtuseUtil;
import com.obtuse.util.gowing.packer2.*;
import com.obtuse.util.gowing.packer2.p2a.util.GowingPackable2Collection;
import com.obtuse.util.gowing.packer2.p2a.util.GowingPackable2Mapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Unpack entities using a purely text-based format (no binary data) and explicitly named fields.
 */

public class StdGowingUnPacker2A implements GowingUnPacker2 {

    public static final long OLDEST_MAJOR_VERSION = 1L;
    public static final long NEWEST_MAJOR_VERSION = 1L;
    public static final long OLDEST_MINOR_VERSION = 1L;
    public static final long NEWEST_MINOR_VERSION = 1L;

    private final GowingTokenizer2 _tokenizer;

    private final GowingUnPackerContext2 _unPackerContext;

    @SuppressWarnings("WeakerAccess")
    public StdGowingUnPacker2A( GowingTypeIndex2 typeIndex, File inputFile )
	    throws IOException {
	this( inputFile, new LineNumberReader( new FileReader( inputFile ) ), new StdGowingUnPackerContext2( typeIndex ) );

    }

    public StdGowingUnPacker2A( GowingTypeIndex2 typeIndex, File inputFile, Reader reader )
	    throws IOException {
	this(
		inputFile,
		reader instanceof LineNumberReader ? (LineNumberReader)reader : new LineNumberReader( reader ),
		new StdGowingUnPackerContext2( typeIndex )
	);

    }

    @SuppressWarnings({ "WeakerAccess", "RedundantThrows" })
    public StdGowingUnPacker2A( File inputFile, LineNumberReader reader, GowingUnPackerContext2 unPackerContext )
	    throws IOException {
	super();

	_unPackerContext = unPackerContext;
	_tokenizer = new GowingTokenizer2( unPackerContext, reader );

	getUnPackerContext().registerFactory( GowingPackable2Mapping.FACTORY );
	getUnPackerContext().registerFactory( GowingPackable2KeyValuePair.FACTORY );
	getUnPackerContext().registerFactory( GowingPackable2Collection.FACTORY );
	getUnPackerContext().registerFactory( EntityName2.getFactory() );
	getUnPackerContext().registerFactory( EntityTypeName2.getFactory() );

    }

    private GowingFormatVersion parseVersion()
	    throws IOException, GowingUnPacker2ParsingException {

	GowingTokenizer2.GowingToken2 versionToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.FORMAT_VERSION );
	@SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 colon = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.COLON );
	GowingTokenizer2.GowingToken2 groupName = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.STRING );
//	P2ATokenizer.GowingToken2 semiColon = _tokenizer.getNextToken( false, P2ATokenizer.TokenType.SEMI_COLON );

	return new GowingFormatVersion( versionToken, new EntityName2( groupName.stringValue() ) );

//	} else {
//
//	    throw new UnPacker2ParseError( "expected the format version, found " + versionToken.getDescription() + " instead", versionToken );
//
//	}

    }

    @Override
    @Nullable
    public GowingDePackedEntityGroup unPack() {

	try {

	    GowingDePackedEntityGroup group;

	    GowingFormatVersion version = parseVersion();
	    @SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 semiColon = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.SEMI_COLON );

	    group = new GowingDePackedEntityGroup( version );

	    while ( true ) {

		GowingTokenizer2.GowingToken2 token = _tokenizer.getNextToken( false );

		if ( token.type() == GowingTokenizer2.TokenType.LONG ) {

		    _tokenizer.putBackToken( token );
		    collectTypeAlias();
		    //noinspection UnusedAssignment
		    semiColon = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.SEMI_COLON );

		} else if ( token.type() == GowingTokenizer2.TokenType.ENTITY_REFERENCE ) {

		    _tokenizer.putBackToken( token );
		    GowingPackedEntityBundle bundle = collectEntityDefinitionClause( false );
		    @SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 semiColonToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.SEMI_COLON );

		    GowingPackable2 entity = constructEntity( token.entityReference(), token, bundle );

		    group.add( token.entityReference().getEntityReferenceNames(), entity );

//		    throw new UnPacker2ParseError( "no support for entity definitions yet", token );
//		    definition = parseEntityDefinition();

		} else if ( token.type() == GowingTokenizer2.TokenType.EOF ) {

		    Logger.logMsg( "EOF reached" );

		    break;

		} else {

		    throw new GowingUnPacker2ParsingException( "unexpected token " + token, token );

		}

	    }

	    Logger.logMsg( "finishing " + group.getAllEntities().size() + " entities" );

	    _unPackerContext.clearUnFinishedEntities();
	    _unPackerContext.markEntitiesUnfinished( _unPackerContext.getSeenEntityReferences() );

	    while ( true ) {

		Collection<GowingEntityReference> unFinishedEntities = _unPackerContext.getUnfinishedEntities();
		if ( unFinishedEntities.isEmpty() ) {

		    Logger.logMsg( "done finishing" );
		    break;

		}

		Logger.logMsg( "starting finishing pass" );

		boolean finishedSomething = false;
		for ( GowingEntityReference er : unFinishedEntities ) {

		    if ( !_unPackerContext.isEntityFinished( er ) ) {

			GowingPackable2 entity = resolveReference( er );
			if ( entity.finishUnpacking( this ) ) {

			    _unPackerContext.markEntityFinished( er );
			    finishedSomething = true;
			    Logger.logMsg( "finished " + er );

			}

		    }

		}

		if ( !finishedSomething ) {

		    throw new HowDidWeGetHereError( "nothing left that can be finished (" + unFinishedEntities.size() + " unfinished entit" + ( unFinishedEntities.size() == 1 ? "y" : "ies" ) + " still unfinished)" );

		}
//		for ( Packable2 entity : group.getAllEntities() ) {
//
//		    entity.finishUnpacking( this );
//
//		}
	    }

	    return group;

	} catch ( GowingUnPacker2ParsingException e ) {

	    Logger.logErr( "error parsing packed entity - " + e.getMessage() + " (" + e.getCauseToken() + ")" );

	    return null;

	} catch ( IOException e ) {

	    Logger.logErr( "I/O error parsing packed entity", e );

	    return null;

	}

    }

    private GowingPackable2 constructEntity( GowingEntityReference er, GowingTokenizer2.GowingToken2 token, GowingPackedEntityBundle bundle )
	    throws GowingUnPacker2ParsingException {

	if ( _unPackerContext.isEntityKnown( er ) ) {

	    throw new GowingUnPacker2ParsingException( "entity with er " + er + " already unpacked during this unpacking session", token );

	}

	EntityTypeName2 typeName = _unPackerContext.findTypeByTypeReferenceId( er.getTypeId() );
	if ( typeName == null ) {

	    throw new GowingUnPacker2ParsingException( "type id " + er.getTypeId() + " not previously defined in data stream", token );

	}

	EntityTypeInfo2 typeInfo = _unPackerContext.findTypeInfo( typeName );
	if ( typeInfo == null ) {

	    throw new GowingUnPacker2ParsingException( "no factory for type id " + er.getTypeId() + " (" + _unPackerContext.findTypeByTypeReferenceId( er.getTypeId() ) + ")", token );

	}

	GowingEntityFactory2 factory = typeInfo.getFactory();

	/*
	Create the entity.
	If something goes wrong, augment the GowingUnPacker2ParsingException with the current token unless the exception already specifies a token.
	In either case, rethrow the exception.
	 */

	GowingPackable2 entity;
	try {

	    entity = factory.createEntity( this, bundle, er );

	} catch ( GowingUnPacker2ParsingException e ) {

	    if ( e.getCauseToken() == null ) {

		e.setCauseToken( token );

	    }

	    throw e;

	}

	_unPackerContext.rememberPackableEntity( token, er, entity );

	return entity;

    }

    @Override
    public GowingPackable2 resolveReference( @Nullable GowingEntityReference er ) {

	if ( er == null ) {

	    return null;

	}

	return _unPackerContext.recallPackableEntity( er );

    }

    public boolean isEntityFinished( GowingEntityReference er ) {

	return _unPackerContext.isEntityFinished( er );

    }

    private void collectTypeAlias()
	    throws IOException, GowingUnPacker2ParsingException {

	GowingTokenizer2.GowingToken2 typeIdToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.LONG );
	@SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 atSignToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.AT_SIGN );
	GowingTokenizer2.GowingToken2 typeNameToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.STRING );
//	P2ATokenizer.GowingToken2 semiColonToken = _tokenizer.getNextToken( false, P2ATokenizer.TokenType.SEMI_COLON );

	_unPackerContext.saveTypeAlias( typeIdToken, typeNameToken );

    }

    @NotNull
    private GowingPackedEntityBundle collectEntityDefinitionClause( boolean parsingSuperClause )
	    throws IOException, GowingUnPacker2ParsingException {

	GowingTokenizer2.GowingToken2 ourEntityReferenceToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.ENTITY_REFERENCE );
	if ( ourEntityReferenceToken.entityReference().getVersion() == null ) {

	    throw new GowingUnPacker2ParsingException( "entity references in entity definitions and super clause definitions must have version ids", ourEntityReferenceToken );

	}

	@SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 equalSignToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.EQUAL_SIGN );
	@SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 leftParenToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.LEFT_PAREN );

	EntityTypeName2 entityTypeName = _unPackerContext.findTypeByTypeReferenceId( ourEntityReferenceToken.entityReference().getTypeId() );
	if ( entityTypeName == null ) {

	    throw new GowingUnPacker2ParsingException( "unknown type id " + ourEntityReferenceToken.entityReference().getTypeId() + " in LHS of entity definition clause", ourEntityReferenceToken );

	}

	if ( !parsingSuperClause && ourEntityReferenceToken.entityReference().getEntityId() == 0 ) {

	    throw new GowingUnPacker2ParsingException( "entity id may only be zero in super clauses", ourEntityReferenceToken );

	} else if ( parsingSuperClause && ourEntityReferenceToken.entityReference().getEntityId() != 0 ) {

	    throw new GowingUnPacker2ParsingException( "entity id must be zero in super clauses", ourEntityReferenceToken );

	}

//	P2ATokenizer.GowingToken2 leftParen = _tokenizer.getNextToken( false, P2ATokenizer.TokenType.LEFT_PAREN );

//	@SuppressWarnings("UnusedAssignment") boolean gotFieldDefinition = false;
	GowingPackedEntityBundle bundle = null;

	while ( true ) {

	    GowingTokenizer2.GowingToken2 token = _tokenizer.getNextToken( true );
	    switch ( token.type() ) {

		case ENTITY_REFERENCE:

		    {
			if ( bundle != null ) {

			    throw new GowingUnPacker2ParsingException( "super clause must be the first clause in an entity definition clause", token );

			}

			// start of the 'super' clause

			_tokenizer.putBackToken( token );
			GowingPackedEntityBundle superClause = collectEntityDefinitionClause( true );

			Integer version = ourEntityReferenceToken.entityReference().getVersion();
			if ( version == null ) {

			    throw new HowDidWeGetHereError( "parsing super clause - should be impossible to get here with a null version number" );

			}

			bundle = new GowingPackedEntityBundle(
				entityTypeName,
				ourEntityReferenceToken.entityReference().getTypeId(),
				superClause,
				version,
				_unPackerContext
			);

		    }

		    break;

		case IDENTIFIER:

		    // start of a field definition clause

		    _tokenizer.putBackToken( token );

		    // If this is the first clause then create our PEB.

		    if ( bundle == null ) {

			Integer version = ourEntityReferenceToken.entityReference().getVersion();
			if ( version == null ) {

			    throw new HowDidWeGetHereError( "first field definition clause - should be impossible to get here with a null version number" );

			}

			bundle = new GowingPackedEntityBundle(
				entityTypeName,
				ourEntityReferenceToken.entityReference().getTypeId(),
				null,
				version,
				_unPackerContext
			);

		    }

		    // Collect the field definition and add it to our PEB.

		    collectFieldDefinitionClause( bundle );

		    break;

		case RIGHT_PAREN:

		    // end of our field value display clause

		    if ( bundle == null ) {

			bundle = new GowingPackedEntityBundle(
				entityTypeName,
				ourEntityReferenceToken.entityReference().getTypeId(),
				null,
				-1,
				_unPackerContext
			);

		    }

		    return bundle;

		case COMMA:

		    // end of this field definition, more to come

//		    gotFieldDefinition = true;

		    break;

	    }

	}

    }

//    private void parseFieldValueDisplayClause()
//	    throws IOException, UnPacker2ParsingException {
//
//	throw new HowDidWeGetHereError( "unimplemented" );
//
////	boolean gotSuper = false;
////
////	P2ATokenizer.GowingToken2 leftParen = _tokenizer.getNextToken( false, P2ATokenizer.TokenType.LEFT_PAREN );
////
////	while ( true ) {
////
////	    P2ATokenizer.GowingToken2 token = _tokenizer.getNextToken( true );
////	    switch ( token.type() ) {
////
////		case LEFT_PAREN:
////
////		    // start of the 'super' clause
////
////		    if ( gotSuper ) {
////
////			throw new UnPacker2ParseError( "unexpected 'super' clause", token );
////
////		    }
////
////		    _tokenizer.putBackToken( token );
////		    superInitializer = parseFieldValueDisplayClause();
////
////		    break;
////
////		case IDENTIFIER:
////
////		    // start of a field definition clause
////		    _tokenizer.putBackToken( token );
////		    fieldDefinitionClause = getFieldDefinitionClause();
////
////		    break;
////
////		case RIGHT_PAREN:
////
////		    // end of our field value display clause
////
////		    return fieldValueDisplayClause;
////		    break;
////
////		case COMMA:
////
////		    // end of this field definition, more to come
////
////		    break;
////
////	    }
////
////	}
//
//    }

    private void collectFieldDefinitionClause( GowingPackedEntityBundle bundle )
	    throws IOException, GowingUnPacker2ParsingException {

//	P2ATokenizer.GowingToken2 equalSize = _tokenizer.getNextToken( false, P2ATokenizer.TokenType.EQUAL_SIGN );
//	valueToken = _tokenizer.getNextToken( false );

	GowingTokenizer2.GowingToken2 identifierToken = _tokenizer.getNextToken( true, GowingTokenizer2.TokenType.IDENTIFIER );
	@SuppressWarnings("UnusedAssignment") GowingTokenizer2.GowingToken2 equalSignToken = _tokenizer.getNextToken( false, GowingTokenizer2.TokenType.EQUAL_SIGN );
	GowingTokenizer2.GowingToken2 valueToken = _tokenizer.getNextToken( false );

	if ( valueToken.type() == GowingTokenizer2.TokenType.ENTITY_REFERENCE && valueToken.entityReference().getVersion() != null ) {

	    throw new GowingUnPacker2ParsingException( "entity reference values must not have version numbers", valueToken );

	}

	GowingPackable2ThingHolder2 holder = valueToken.createHolder( identifierToken.identifierValue(), valueToken, _unPackerContext );

	Logger.logMsg( "got field definition:  " + identifierToken.identifierValue() + " = " + valueToken.getObjectValue() );

	if ( bundle.containsKey( holder.getName() ) ) {

	    throw new GowingUnPacker2ParsingException( "more than one field named \"" + identifierToken.identifierValue() + "\"", identifierToken );

	}

	bundle.put( holder.getName(), holder );

    }

    @Override
    public GowingUnPackerContext2 getUnPackerContext() {

	return _unPackerContext;

    }

    public static void main( String[] args ) {

	BasicProgramConfigInfo.init( "Obtuse", "Packer", "testing", null );

	try {

	    GowingUnPacker2 unPacker = new StdGowingUnPacker2A( new GowingTypeIndex2( "test unpacker" ), new File( "test1.p2a" ) );

	    unPacker.getUnPackerContext().registerFactory( StdGowingPackerContext2.TestPackableClass.FACTORY );
	    unPacker.getUnPackerContext().registerFactory( StdGowingPackerContext2.SimplePackableClass.FACTORY );
	    unPacker.getUnPackerContext().registerFactory( StdGowingPacker2A.SortedSetExample.FACTORY );

	    GowingDePackedEntityGroup result = unPacker.unPack();

	    if ( result != null ) {

		for ( GowingPackable2 entity : result.getAllEntities() ) {

		    Logger.logMsg( "got " + entity );

		}

	    }

	    ObtuseUtil.doNothing();

	} catch ( IOException e ) {

	    Logger.logErr( "unable to create StdGowingUnPacker2A instance", e );

	}

    }

}
