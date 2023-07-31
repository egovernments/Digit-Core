import React from "react";
import { EventSeat } from "./EventSeat";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EventSeat",
  component: EventSeat,
};

export const Default = () => <EventSeat />;
export const Fill = () => <EventSeat fill="blue" />;
export const Size = () => <EventSeat height="50" width="50" />;
export const CustomStyle = () => <EventSeat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EventSeat className="custom-class" />;

export const Clickable = () => <EventSeat onClick={()=>console.log("clicked")} />;

const Template = (args) => <EventSeat {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
