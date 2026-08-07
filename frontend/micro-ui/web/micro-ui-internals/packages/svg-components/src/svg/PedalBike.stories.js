import React from "react";
import { PedalBike } from "./PedalBike";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PedalBike",
  component: PedalBike,
};

export const Default = () => <PedalBike />;
export const Fill = () => <PedalBike fill="blue" />;
export const Size = () => <PedalBike height="50" width="50" />;
export const CustomStyle = () => <PedalBike style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PedalBike className="custom-class" />;

export const Clickable = () => <PedalBike onClick={()=>console.log("clicked")} />;

const Template = (args) => <PedalBike {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
