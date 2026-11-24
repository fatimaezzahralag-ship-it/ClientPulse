import {
  afterRenderEffect,
  Component,
  ElementRef,
  input,
  OnDestroy,
  viewChild
} from '@angular/core';

import {
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  ChartData,
  ChartOptions,
  ChartType,
  DoughnutController,
  Filler,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip
} from 'chart.js';

// Enregistrement sélectif : n'embarque que les types de graphiques utilisés (bundle plus léger).
Chart.register(
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  DoughnutController,
  Filler,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip
);

/**
 * Enveloppe Angular de Chart.js : le graphique est recréé quand les données changent.
 *
 *   <app-chart type="bar" [data]="donnees()" [options]="options" />
 *
 * Le parent fixe la hauteur (le canvas remplit son conteneur).
 */
@Component({
  selector: 'app-chart',
  template: `<div class="chart-wrap"><canvas #canvas></canvas></div>`,
  styles: `
    :host { display: block; }
    .chart-wrap { position: relative; width: 100%; height: 100%; min-height: 220px; }
  `
})
export class ChartComponent implements OnDestroy {

  readonly type = input.required<ChartType>();
  readonly data = input.required<ChartData>();
  readonly options = input<ChartOptions>({});

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private graphique: Chart | null = null;

  constructor() {
    // Après chaque rendu où l'un des signaux lus a changé : on reconstruit le graphique.
    afterRenderEffect(() => {
      const type = this.type();
      const data = this.data();
      const options = this.options();

      this.graphique?.destroy();
      this.graphique = new Chart(this.canvas().nativeElement, {
        type,
        data,
        options: { responsive: true, maintainAspectRatio: false, ...options }
      });
    });
  }

  ngOnDestroy(): void {
    this.graphique?.destroy();
    this.graphique = null;
  }
}
