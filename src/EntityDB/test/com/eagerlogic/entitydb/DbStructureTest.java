/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eagerlogic.entitydb;

import com.eagerlogic.entitydb.DbFileStructure;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dipacs
 */
public class DbStructureTest {
	
	private DbFileStructure structure;
	
	public DbStructureTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		structure = new DbFileStructure(2);
		for (int i = 0; i < 1000; i++) {
			structure.addPiece(i * 10 + 2, 4);
		}
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testFreeSpace() {
		// checking first empty space
		long[] offsets = structure.getEmptySpace(6);
		long prevOff = offsets[0];
		long off = offsets[1];
		long nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 2);
		assertTrue("Offset wrong!", off == 6);
		assertTrue("Next offset wrong!", nextOff == 12);
		
		// checking unfit empty space
		structure.addPiece(off, 4);
		offsets = structure.getEmptySpace(3);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 12);
		assertTrue("Offset wrong!", off == 16);
		assertTrue("Next offset wrong!", nextOff == 22);
		
		// checking end empty space
		offsets = structure.getEmptySpace(100);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 999 * 10 + 2);
		assertTrue("Offset wrong!", off == 999 * 10 + 2 + 4);
		assertTrue("Next offset wrong!", nextOff == -1);
		
		// checking empty space after remove
		structure.removePiece(998 * 10 + 2);
		offsets = structure.getEmptySpace(16);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 997 * 10 + 2);
		assertTrue("Offset wrong!", off == 997 * 10 + 2 + 4);
		assertTrue("Next offset wrong!", nextOff == 999 * 10 + 2);
	}
	
	@Test
	public void testSurround() {
		// check first piece
		long[] offsets = structure.getPieceSurround(2);
		long prevOff = offsets[0];
		long off = offsets[1];
		long nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == -1);
		assertTrue("Offset wrong!", off == 2);
		assertTrue("Next offset wrong!", nextOff == 12);
		
		// check last piece
		offsets = structure.getPieceSurround(999 * 10 + 2);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 998 *10 + 2);
		assertTrue("Offset wrong!", off == 999 * 10 + 2);
		assertTrue("Next offset wrong!", nextOff == -1);
		
		// check inner piece
		offsets = structure.getPieceSurround(500 * 10 + 2);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 499 *10 + 2);
		assertTrue("Offset wrong!", off == 500 * 10 + 2);
		assertTrue("Next offset wrong!", nextOff == 501 * 10 + 2);
	}
	
	@Test
	public void testSurroundAfterDelete() {
		// remove first piece
		structure.removePiece(2);
		long[] offsets = structure.getPieceSurround(1 * 10 + 2);
		long prevOff = offsets[0];
		long off = offsets[1];
		long nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == -1);
		assertTrue("Offset wrong!", off == 1 * 10 + 2);
		assertTrue("Next offset wrong!", nextOff == 2 * 10 + 2);
		
		// remove last piece
		structure.removePiece(999 * 10 + 2);
		offsets = structure.getPieceSurround(998 * 10 + 2);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 997 * 10 + 2);
		assertTrue("Offset wrong!", off == 998 * 10 + 2);
		assertTrue("Next offset wrong!", nextOff == -1);
		
		// remove inner piece
		structure.removePiece(600 * 10 + 2);
		offsets = structure.getPieceSurround(599 * 10 + 2);
		prevOff = offsets[0];
		off = offsets[1];
		nextOff = offsets[2];
		assertTrue("Previous offset wrong!", prevOff == 598 * 10 + 2);
		assertTrue("Offset wrong!", off == 599 * 10 + 2);
		assertTrue("Next offset wrong!", nextOff == 601 * 10 + 2);
	}
	
	@Test
	public void testSurroundAfterInsert() {
		// insert first
		// insert last
		// insert inner
	}
}