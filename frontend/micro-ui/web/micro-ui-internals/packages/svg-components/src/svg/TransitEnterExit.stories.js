import React from "react";
import { TransitEnterExit } from "./TransitEnterExit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TransitEnterExit",
  component: TransitEnterExit,
};

export const Default = () => <TransitEnterExit />;
export const Fill = () => <TransitEnterExit fill="blue" />;
export const Size = () => <TransitEnterExit height="50" width="50" />;
export const CustomStyle = () => <TransitEnterExit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TransitEnterExit className="custom-class" />;
export const Clickable = () => <TransitEnterExit onClick={()=>console.log("clicked")} />;

const Template = (args) => <TransitEnterExit {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

