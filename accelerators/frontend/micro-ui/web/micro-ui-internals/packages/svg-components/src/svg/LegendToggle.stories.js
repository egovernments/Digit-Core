import React from "react";
import { LegendToggle } from "./LegendToggle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LegendToggle",
  component: LegendToggle,
};

export const Default = () => <LegendToggle />;
export const Fill = () => <LegendToggle fill="blue" />;
export const Size = () => <LegendToggle height="50" width="50" />;
export const CustomStyle = () => <LegendToggle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LegendToggle className="custom-class" />;

export const Clickable = () => <LegendToggle onClick={()=>console.log("clicked")} />;

const Template = (args) => <LegendToggle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
