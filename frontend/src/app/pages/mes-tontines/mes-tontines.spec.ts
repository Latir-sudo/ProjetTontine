import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesTontines } from './mes-tontines';

describe('MesTontines', () => {
  let component: MesTontines;
  let fixture: ComponentFixture<MesTontines>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesTontines],
    }).compileComponents();

    fixture = TestBed.createComponent(MesTontines);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
