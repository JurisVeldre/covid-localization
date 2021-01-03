/**
 * LEAPS - Low Energy Accurate Positioning System.
 *
 * Simple user application.
 *
 * Copyright (c) 2016-2019, LEAPS. All rights reserved.
 *
 */

#include "dwm.h"
#include <stdio.h>
#include <string.h>
#include <time.h>
/* Thread priority */
#ifndef THREAD_APP_PRIO
#define THREAD_APP_PRIO 20
#endif /* THREAD_APP_PRIO */

/* Thread stack size */
#ifndef THREAD_APP_STACK_SIZE
#define THREAD_APP_STACK_SIZE (3 * 1024)
#endif /* THREAD_APP_STACK_SIZE */

#define APP_ERR_CHECK(err_code)                               \
  do {                                                        \
    if ((err_code) != DWM_OK)                                 \
      printf("err: line(%u) code(%u)", __LINE__, (err_code)); \
  } while (0)

#define MSG_INIT                         \
  "\n\n"                                 \
  "App   :  dwm-simple\n"                \
  "Built :  " __DATE__ " " __TIME__ "\n" \
  "\n"

long long counter = 0;

/**
 * Event callback
 *
 * @param[in] p_evt  Pointer to event structure
 */
void on_dwm_evt(dwm_evt_t *p_evt) {
  int len;
  int i;

  // Data to send in IoT data
  uint8_t data_out[34] = {0};
  // Data to send in IoT data
  uint8_t data_in[34] = {0};
  // Current Idc to store in data
  uint8_t idx = 0;
  uint16_t static cnt_data_out = 0;
  int counter = 0;

  time_t rawtime;
  struct tm *timeinfo;

  switch (p_evt->header.id) {
  /* New location data */
  case DWM_EVT_LOC_READY:
    counter = counter + 1;
    if (counter >= 100) {
      counter = 0;
    }

    printf("\nT:%lu ", dwm_systime_us_get());
    if (p_evt->loc.pos_available) {
      printf("POS:[%ld,%ld,%ld,%u] ", p_evt->loc.pos.x,
          p_evt->loc.pos.y, p_evt->loc.pos.z,
          p_evt->loc.pos.qf);
    } else {
      printf("POS:N/A ");
    }

    for (i = 0; i < p_evt->loc.anchors.dist.cnt; ++i) {
      printf("DIST%d:", i);

      printf("0x%04X", (unsigned int)(p_evt->loc.anchors.dist.addr[i] & 0xffff));
      if (i < p_evt->loc.anchors.an_pos.cnt) {
        printf("[%ld,%ld,%ld]",
            p_evt->loc.anchors.an_pos.pos[i].x,
            p_evt->loc.anchors.an_pos.pos[i].y,
            p_evt->loc.anchors.an_pos.pos[i].z);
      }

      printf("=[%lu,%u] ", p_evt->loc.anchors.dist.dist[i],
          p_evt->loc.anchors.dist.qf[i]);
    }

    data_out[0] = (uint8_t)p_evt->loc.anchors.dist.cnt;

    int data_counter = 1;

    // Coding location and address data into 34 bytes
    for (int i = 0; i < p_evt->loc.anchors.dist.cnt; i++) {
      // address data_out[1] and data_out[2]
      data_out[data_counter + 0] = (uint16_t)p_evt->loc.anchors.dist.addr[i] & 0x000000FFu;
      data_out[data_counter + 1] = (((uint16_t)p_evt->loc.anchors.dist.addr[i]) & 0x0000FF00u) >> 8;

      // distance data_out[1] and data_out[2]
      data_out[data_counter + 2] = (uint16_t)p_evt->loc.anchors.dist.dist[i];
      data_out[data_counter + 3] = (((uint16_t)p_evt->loc.anchors.dist.dist[i] & 0x0000FF00u)) >> 8;

      printf("\nPutting data into: %d; %d; %d; %d \n", data_counter + 0, data_counter + 1, data_counter + 2, data_counter + 3);
      data_counter = data_counter + 3 + 1;
    }

    data_out[data_counter] = (uint8_t)counter;

    dwm_evt_listener_register(
        DWM_EVT_LOC_READY | DWM_EVT_USR_DATA_SENT |
            DWM_EVT_BH_INITIALIZED_CHANGED |
            DWM_EVT_UWBMAC_JOINED_CHANGED,
        NULL);

    dwm_usr_data_write(data_out, 34, false);
    break;

  case DWM_EVT_USR_DATA_READY:
    len = p_evt->header.len - sizeof(dwm_evt_hdr_t);
    if (len <= 0)
      break;

    printf("iot received, len=%d:", len);
    for (i = 0; i < len; ++i) {
      printf(" %02X", p_evt->usr_data[i]);
    }
    break;

  case DWM_EVT_USR_DATA_SENT:
    printf("iot sent\n");
    break;

  case DWM_EVT_BH_INITIALIZED_CHANGED:
    printf("uwbmac: backhaul = %d\n", p_evt->bh_initialized);
    break;

  case DWM_EVT_UWBMAC_JOINED_CHANGED:
    printf("uwbmac: joined = %d\n", p_evt->uwbmac_joined);
    break;

  default:
    break;
  }
}

/**
 * Application thread
 *
 * @param[in] data  Pointer to user data
 */
void app_thread_entry(uint32_t data) {
  dwm_cfg_t cfg;
  uint8_t i2cbyte;
  dwm_evt_t evt;
  int rv;
  uint8_t label[DWM_LABEL_LEN_MAX];
  uint8_t label_len = DWM_LABEL_LEN_MAX;
  dwm_pos_t pos;

  /* Initial message */
  printf(MSG_INIT);

  /* Get node configuration */
  APP_ERR_CHECK(dwm_cfg_get(&cfg));

  /* Update rate set to 1 second, stationary update rate set to 5 seconds */
  APP_ERR_CHECK(dwm_upd_rate_set(10, 10));

  /* Sensitivity for switching between stationary and normal update rate */
  APP_ERR_CHECK(dwm_stnry_cfg_set(DWM_STNRY_SENSITIVITY_NORMAL));

  /* Register event callback */
  dwm_evt_listener_register(
      DWM_EVT_LOC_READY | DWM_EVT_USR_DATA_READY |
          DWM_EVT_BH_INITIALIZED_CHANGED |
          DWM_EVT_UWBMAC_JOINED_CHANGED,
      NULL);

  /* Test the accelerometer */
  i2cbyte = 0x0f;
  rv = dwm_i2c_write(0x33 >> 1, &i2cbyte, 1, true);

  if (rv == DWM_OK) {
    rv = dwm_i2c_read(0x33 >> 1, &i2cbyte, 1);

    if (rv == DWM_OK) {
      printf("Accelerometer chip ID: %u\n", i2cbyte);
    } else {
      printf("i2c: read failed (%d)\n", rv);
    }
  } else {
    printf("i2c: write failed (%d)\n", rv);
  }

  rv = dwm_label_read(label, &label_len);

  if (rv == DWM_OK) {
    printf("LABEL(len=%d):", label_len);
    for (rv = 0; rv < label_len; ++rv) {
      printf(" %02x", label[rv]);
    }
    printf("\n");
  } else {
    printf("can't read label len=%d, error %d\n", label_len, rv);
  }

  while (1) {
    /* Thread loop */
    rv = dwm_evt_wait(&evt);

    if (rv != DWM_OK) {
      printf("dwm_evt_wait, error %d\n", rv);
    } else {
      on_dwm_evt(&evt);
    }
  }
}

/**
 * Application entry point. Initialize application thread.
 *
 * @warning ONLY ENABLING OF LOCATION ENGINE OR BLE AND CREATION AND STARTING OF
 * USER THREADS CAN BE DONE IN THIS FUNCTION
 */
void dwm_user_start(void) {
  uint8_t hndl;
  int rv;

  dwm_shell_compile();
  //Disabling ble by default as softdevice prevents debugging with breakpoints (due to priority)
  //dwm_ble_compile();
  dwm_le_compile();
  dwm_serial_spi_compile();

  /* Create thread */
  rv = dwm_thread_create(THREAD_APP_PRIO, app_thread_entry, (void *)NULL,
      "app", THREAD_APP_STACK_SIZE, &hndl);
  APP_ERR_CHECK(rv);

  /* Start the thread */
  dwm_thread_resume(hndl);
}