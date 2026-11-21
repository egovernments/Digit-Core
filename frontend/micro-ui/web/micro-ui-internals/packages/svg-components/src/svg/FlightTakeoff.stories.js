import React from "react";
import { FlightTakeoff } from "./FlightTakeoff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FlightTakeoff",
  component: FlightTakeoff,
};

export const Default = () => <FlightTakeoff />;
export const Fill = () => <FlightTakeoff fill="blue" />;
export const Size = () => <FlightTakeoff height="50" width="50" />;
export const CustomStyle = () => <FlightTakeoff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlightTakeoff className="custom-class" />;

export const Clickable = () => <FlightTakeoff onClick={()=>console.log("clicked")} />;

const Template = (args) => <FlightTakeoff {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
