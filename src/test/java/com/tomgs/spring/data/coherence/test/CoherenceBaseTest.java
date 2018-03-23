package com.tomgs.spring.data.coherence.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:application-cache-coherence.xml")
public class CoherenceBaseTest extends AbstractJUnit4SpringContextTests {

}
