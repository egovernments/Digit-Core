import React from "react";
import { TransferWithinStation } from "./TransferWithinStation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TransferWithinStation",
  component: TransferWithinStation,
};

export const Default = () => <TransferWithinStation />;
export const Fill = () => <TransferWithinStation fill="blue" />;
export const Size = () => <TransferWithinStation height="50" width="50" />;
export const CustomStyle = () => <TransferWithinStation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TransferWithinStation className="custom-class" />;
export const Clickable = () => <TransferWithinStation onClick={()=>console.log("clicked")} />;

const Template = (args) => <TransferWithinStation {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

